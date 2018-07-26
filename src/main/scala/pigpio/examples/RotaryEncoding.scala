package pigpio.examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Timers}
import pigpio.scaladsl.GpioImplicits._
import pigpio.scaladsl.GpioPin.Listen
import pigpio.scaladsl._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/*
             +---------+         +---------+      0
             |         |         |         |
   A         |         |         |         |
             |         |         |         |
   +---------+         +---------+         +----- 1
       +---------+         +---------+            0
       |         |         |         |
   B   |         |         |         |
       |         |         |         |
   ----+         +---------+         +---------+  1

 REQUIRES
A rotary encoder contacts A and B connected to separate gpios and
the common contact connected to Pi ground.


 */
object RotaryEncoding extends App {
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

  println("Example of reading rotary encoder on pins 20/21")
  system.actorOf(Encoder.props(20, 21))

  Await.ready(system.whenTerminated, Duration.Inf)
}

object Encoder {
  def props(p1: UserGpio, p2: UserGpio)(implicit lgpio: PigpioLibrary) =
    Props(new Encoder(p1, p2))
}

class Encoder(p1: UserGpio, p2: UserGpio)(implicit lgpio: PigpioLibrary)
    extends Actor
    with Timers {

  val pin1: ActorRef = context.actorOf(GpioPin.props(p1))
  val pin2: ActorRef = context.actorOf(GpioPin.props(p2))

  var l1: Level = Low
  var l2: Level = Low
  var prev: UserGpio = 0

  pin1 ! InputPin
  pin2 ! InputPin

  pin1 ! PullUp
  pin2 ! PullUp

  pin1 ! Listen()
  pin2 ! Listen()

  def receive: Receive = {
    case GpioAlert(p, l, t) if p != prev ⇒
      p match {
        case `p1` if l.toBoolean && l2.toBoolean ⇒
          println("1")
        case `p2` if l.toBoolean && l1.toBoolean ⇒
          println("-1")
      }
      prev = p
  }
}
