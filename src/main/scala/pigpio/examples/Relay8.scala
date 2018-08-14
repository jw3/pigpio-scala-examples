package pigpio.examples

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}
import pigpio.examples.R8.{AllOpen, R, Rs}
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

  val lower = system.actorOf(R8.props(0), "stack0")
  val upper = system.actorOf(R8.props(1), "stack1")

  Thread.sleep(2000)

  println("all off")
  lower ! AllOpen
  upper ! AllOpen

  println("sequential")

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

  // all off again

  println("concurrent")

  val on = Rs(List(R(1, Low), R(2, Low), R(3, Low)))
  val off = Rs(List(R(1, High), R(2, High), R(3, High)))

  lower ! on
  Thread.sleep(1000)
  upper ! on
  Thread.sleep(1000)
  lower ! off
  Thread.sleep(1000)
  upper ! off

  println("done")
}

object R8 {
  def props(stack: Int)(implicit lgpio: PigpioLibrary) =
    Props(new R8(stack))

  val masks = Array(0x01, 0x02, 0x04, 0x08, 0x80, 0x40, 0x20, 0x10)
  val channels = Array(0, 1, 2, 3, 7, 6, 5, 4)

  val RELAY8_I2C_DEV_ADDRESS = 0x20
  val RELAY8_OUTPORT_REG_ADD = 0x01
  val AllOpenState = 0

  def device(stack: Int): Int = 0x07 ^ stack
  def address(stack: Int): Int = RELAY8_I2C_DEV_ADDRESS + device(stack)
  def i2c(stack: Int)(implicit lgpio: PigpioLibrary): Int =
    lgpio.i2cOpen(1, address(stack), 0)

  def mask(r: Int, v: Int, l: Level): Int = l match {
    case High ⇒ v & ~(1 << R8.channels(r - 1))
    case Low ⇒ v | 1 << R8.channels(r - 1)
  }

  case object AllOpen
  case class R(channel: Int, level: Level)
  case class Rs(rs: Seq[R])
}

class R8(stack: Int)(implicit lgpio: PigpioLibrary)
    extends Actor
    with Stash
    with ActorLogging {

  def initialized(i2c: Int, state: Int): Receive = {
    unstashAll()

    {
      case R(ch, lvl: Level) ⇒
        context become writing(i2c, R8.mask(ch, state, lvl))

      case Rs(rs) ⇒
        context become writing(
          i2c,
          rs.foldLeft(state)((l, r) ⇒ R8.mask(r.channel, l, r.level))
        )

      case AllOpen ⇒
        context become writing(i2c, R8.AllOpenState)
    }
  }

  def writing(i2c: Int, v: Int): Receive = {
    log.info("writing {}", v)
    write(i2c, v)
    context.become(initialized(i2c, v))

    {
      case _ ⇒ stash()
    }
  }

  def receive: Receive = {
    val i2c = R8.i2c(stack)
    log.info("initialized i2c {}", i2c)
    context.become(initialized(i2c, R8.AllOpenState))

    {
      case _ ⇒ stash()
    }
  }

  def write(i2c: Int, state: Int): Int =
    lgpio.i2cWriteByteData(i2c, R8.RELAY8_OUTPORT_REG_ADD, state)
}