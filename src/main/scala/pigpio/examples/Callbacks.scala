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
  println("Example of reading callbacks from pin #4")

  implicit val system: ActorSystem = ActorSystem("Callbacks")
  implicit val lgpio = PigpioLibrary.INSTANCE

  // initialize pigpio
  val ver = lgpio.gpioInitialise()
  println(s"Initialized pigpio v$ver")

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
  def receive: Receive = {
    case m: GpioAlert =>
      println(m)
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
