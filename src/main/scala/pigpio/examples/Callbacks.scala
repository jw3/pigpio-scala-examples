package pigpio.examples

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Timers}
import pigpio.scaladsl.GpioImplicits._
import pigpio.scaladsl.GpioPin.Listen
import pigpio.scaladsl.{GpioAlert, _}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

/**
  * Simple demonstration of an Actor based GPIO Callback
  */
object Callbacks extends App {
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

  println("Example of reading callbacks from pin #4")

  // create io pins
  val in = system.actorOf(GpioPin.props(4))
  val out = system.actorOf(GpioPin.props(3))

  // create a listener and subscribe to callbacks
  val listener = system.actorOf(Props[PrintingActor])
  in.tell(Listen(), listener)

  // configure pins
  in ! InputPin
  out ! OutputPin

  // schedule flipping events on output pin
  system.actorOf(Flipper.props(out))

  Await.ready(system.whenTerminated, Duration.Inf)
}

/**
  * The Actor receiving the callback
  */
class PrintingActor extends Actor {
  var cnt = 0

  def receive: Receive = {
    case m: GpioAlert =>
      cnt += 1

      if (cnt % 100 == 0) print(".")
      if (cnt % 1000 == 0) print(cnt / 1000)
  }
}

object Flipper {
  def props(pin: ActorRef) = Props(new Flipper(pin))
}

class Flipper(pin: ActorRef) extends Actor with Timers {
  var level: Level = Low

  timers.startPeriodicTimer("flip", Done, 100.millis)

  def receive: Receive = {
    case Done ⇒
      pin ! level
      level = Level.flip(level)
  }
}
