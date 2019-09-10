package pigpio.examples

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import pigpio.examples.Counter.{Cb, Go}
import pigpio.scaladsl.GpioImplicits._
import pigpio.scaladsl.{GpioPin, OutputPin, PigpioLibrary, _}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

object PwmCounter extends App {
  implicit val system: ActorSystem = ActorSystem("pigpio-example")
  implicit val lgpio: PigpioLibrary = PigpioLibrary.INSTANCE

  // initialize pigpio
  lgpio.gpioInitialise() match {
    case PigpioLibrary.PI_INIT_FAILED ⇒
      println("pigpio init failed")
      val f = system.terminate()
      println("terminating actor system")
      Await.ready(f, Duration.Inf)
      System.exit(1)

    case ver ⇒
      println(s"initialized pigpio v$ver")
  }

  println(s".:| Example of counting pwm pulses |:.")

  val opin = 24 // pwm
  val dpin = 25 // direction

  // create io pins
  val out = GpioPin(opin)
  val dir = GpioPin(dpin)

  // configure pins
  out ! OutputPin
  dir ! OutputPin

  // set the direction
  val d = args.headOption match {
    case Some("l") | None ⇒ High
    case Some("r") ⇒ Low
    case _ ⇒ throw new IllegalArgumentException("")
  }

  dir ! d

  val counter = system.actorOf(Counter.props(1000, opin))
  val listener = new GpioAlertFunc(counter)
  lgpio.gpioSetAlertFunc(opin, listener)

  counter ! Go

  implicit val to = Timeout(1000.seconds)
  val done = counter ? Cb

  Await.ready(done, Duration.Inf)

  system.terminate()
  lgpio.gpioTerminate()
}

object Counter {
  type Direction = Level
  val Left: Direction = High
  val Right: Direction = Low

  case object Cb
  case object Go

  def props(c: Int, o: Int)(implicit lgpio: PigpioLibrary) = Props(new Counter(c, o, lgpio))
}

class Counter(c: Int, o: Int, lgpio: PigpioLibrary) extends Actor {
  var cnt = 0
  var cb: Option[ActorRef] = None

  def receive: Receive = {
    case Go ⇒
      // set frequency
      lgpio.gpioSetPWMfrequency(o, 400)
      // move at half speed
      lgpio.gpioPWM(o, 128)

    case Cb ⇒
      cb = Some(sender())

    case GpioAlert(p, l, t) =>
      cnt += 1

      if (cnt > c) {
        lgpio.gpioPWM(o, 0)
        self ! PoisonPill
      }

      if (cnt % 100 == 0) print(".")
      if (cnt % 1000 == 0) print(cnt / 1000)
  }

  override def postStop(): Unit = {
    println(s" => $cnt")
    cb.foreach(_ ! Done)
  }
}
