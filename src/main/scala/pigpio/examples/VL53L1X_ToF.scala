package pigpio.examples

import akka.actor.ActorSystem
import pigpio.scaladsl.PigpioLibrary

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object VL53L1X_ToF extends App {
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

  println(s".:| Example of reading time of flight on i2c |:.")

}
