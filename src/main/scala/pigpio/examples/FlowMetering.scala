package pigpio.examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Timers}
import pigpio.scaladsl.GpioImplicits._
import pigpio.scaladsl.GpioPin.Listen
import pigpio.scaladsl._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationLong

/**
  *
    r ------------ 5v
               +-- 3v3
               |
              4k7
               |
    y ------------ p27
    b ------------ g
  *
  */
object FlowMetering extends App {
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

  val p1 = 27

  println(s".:| Example of reading flow sensor on pin $p1 |:.")
  system.actorOf(FlowMeter.props(p1))

  Await.ready(system.whenTerminated, Duration.Inf)
}

object FlowMeter {
  def props(p1: UserGpio)(implicit lgpio: PigpioLibrary) =
    Props(new FlowMeter(p1))

  case object Print
  val pulsesPerLiter: Double = 5880
}

class FlowMeter(p1: UserGpio)(implicit lgpio: PigpioLibrary)
    extends Actor
    with Timers {

  val pin1: ActorRef = context.actorOf(GpioPin.props(p1))

  pin1 ! Listen()
  pin1 ! InputPin
  pin1 ! PullUp

  var c = 0
  var a = 0D
  var p = 0L

  def receive: Receive = {
    case GpioAlert(_, _, t) ⇒
      val k = t.micros.toSeconds
      c += 1

      if (k != p) {
        a += c / FlowMeter.pulsesPerLiter
        println(s"${a}l")
        p = k
        c = 0
      }
  }
}
