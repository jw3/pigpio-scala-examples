package pigpio.examples

import akka.actor.ActorSystem
import pigpio.scaladsl.PigpioLibrary

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Stepper extends App {

  val fullStepSequence = Array(
    Array(1, 0, 0, 0),
    Array(0, 1, 0, 0),
    Array(0, 0, 1, 0),
    Array(0, 0, 0, 1)
  )

  val halfStepSequence = Array(
    Array(1, 0, 0, 0),
    Array(1, 1, 0, 0),
    Array(0, 1, 0, 0),
    Array(0, 1, 1, 0),
    Array(0, 0, 1, 0),
    Array(0, 0, 1, 1),
    Array(0, 0, 0, 1),
    Array(1, 0, 0, 1)
  )

  implicit val system: ActorSystem = ActorSystem("pigpio-stepper-example")
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

  val p1 = 24
  val p2 = 25
  val p3 = 26
  val p4 = 27

  println(s".:| Example of controlling stepper |:.")
}
