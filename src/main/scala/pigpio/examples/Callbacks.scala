package pigpio.examples

import akka.actor.{Actor, ActorSystem, Props}
import pigpio.scaladsl.GpioImplicits._
import pigpio.scaladsl.GpioPin.Listen
import pigpio.scaladsl.{GpioAlert, _}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Simple demonstration of an Actor based GPIO Callback
 */
object Callbacks extends App {
  println("Example of reading callbacks from pin #4")

  implicit val system: ActorSystem = ActorSystem("Callbacks")
  implicit val lgpio = PigpioLibrary.INSTANCE

  // initialize pigpio
  val ver = lgpio.gpioInitialise
  println(s"Initialized pigpio v$ver")

  // create a pin
  val pin4 = system.actorOf(GpioPin.props(4))

  // create a listener
  val listener = system.actorOf(Props[PrintingActor])

  // listener subscribes to callbacks
  pin4.tell(Listen(), listener)

  // enable input on the pin
  pin4 ! InputPin

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
