package pigpio.examples

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import pigpio.examples.R8.R
import pigpio.scaladsl._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Relay8 extends App {
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


  val lower = system.actorOf(R8.props(0))
  val upper = system.actorOf(R8.props(1))

  lower ! R(3, Low)
  Thread.sleep(1000)
  lower ! R(2, Low)
  Thread.sleep(1000)
  lower ! R(1, Low)

  Thread.sleep(1000)

  upper ! R(3, Low)
  lower ! R(1, High)
  Thread.sleep(1000)
  upper ! R(2, Low)
  lower ! R(2, High)
  Thread.sleep(1000)
  upper ! R(1, Low)
  lower ! R(3, High)

  Thread.sleep(1000)

  upper ! R(1, High)
  Thread.sleep(1000)
  upper ! R(2, High)
  Thread.sleep(1000)
  upper ! R(3, High)
}

object R8 {
  def props(stack: Int)(implicit lgpio: PigpioLibrary) =
    Props(new R8(stack))

  val relayMaskRemap = Array(0x01, 0x02, 0x04, 0x08, 0x80, 0x40, 0x20, 0x10)
  val relayChRemap = Array(0, 1, 2, 3, 7, 6, 5, 4)

  val RELAY8_I2C_DEV_ADDRESS = 0x20
  val RELAY8_OUTPORT_REG_ADD = 0x01

  def device(stack: Int): Int = 0x07 ^ stack
  def address(stack: Int): Int = RELAY8_I2C_DEV_ADDRESS + device(stack)
  def i2c(stack: Int)(implicit lgpio: PigpioLibrary): Int =
    lgpio.i2cOpen(1, address(stack), 0)

  case class R(id: Int, l: Level)
}

class R8(stack: Int)(implicit lgpio: PigpioLibrary) extends Actor with ActorLogging {
  private val i2c = R8.i2c(stack)

  var v: Int = 0

  def receive: Receive = {
    case R(c, High) ⇒
      v &= ~(1 << R8.relayChRemap(c - 1))
      log.info("{} high, writing {}", c, v)
      lgpio.i2cWriteByteData(i2c, R8.RELAY8_OUTPORT_REG_ADD, v)

    case R(c, Low) ⇒
      v |= 1 << R8.relayChRemap(c - 1)
      log.info("{} low, writing {}", c, v)
      lgpio.i2cWriteByteData(i2c, R8.RELAY8_OUTPORT_REG_ADD, v)
  }
}
