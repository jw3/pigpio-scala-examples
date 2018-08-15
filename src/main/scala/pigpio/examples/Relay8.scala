package pigpio.examples

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}
import pigpio.examples.R8.{AllOpen, Handle, R, Rs}
import pigpio.scaladsl._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

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

  val relays = 1 to 8

  val stacks = 0 to 3 map { i ⇒
    system.actorOf(R8.props(i), s"stack-$i")
  }

  val allOn = Rs(relays.map(id ⇒ R(id, Low)))
  val allOff = Rs(relays.map(id ⇒ R(id, High)))

  println("test bulk on")
  stacks.foreach(_ ! allOn)

  println("test bulk off")
  stacks.foreach(_ ! allOff)

  Thread.sleep(2000)

  println("flip everyone on")
  stacks
    .map(_ → relays)
    .foreach(rr ⇒ rr._2.foreach(relay ⇒ rr._1 ! R(relay, Low)))

  Thread.sleep(2000)

  println("flip everyone off ;)")
  stacks
    .map(_ → relays)
    .foreach(rr ⇒ rr._2.foreach(relay ⇒ rr._1 ! R(relay, High)))

  println("done")

  system.terminate()
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
  case class Handle(i2c: Int)
}

class R8(stack: Int)(implicit lgpio: PigpioLibrary)
    extends Actor
    with Stash
    with ActorLogging {

  import context.dispatcher

  def receive: Receive = {
    init().foreach(self ! _)

    {
      case Handle(i2c) ⇒
        unstashAll()
        context become initialized(i2c, R8.AllOpenState)

      case _ ⇒ stash()
    }
  }

  def initialized(i2c: Int, state: Int): Receive = {
    log.debug("i2c {} is {}", i2c, state)

    {
      case R(ch, lvl: Level) ⇒
        log.info("writing {} {}", ch, lvl)
        context become writing(i2c, R8.mask(ch, state, lvl))

      case Rs(rs) ⇒
        log.info("bulk write")
        context become writing(
          i2c,
          rs.foldLeft(state)((l, r) ⇒ R8.mask(r.channel, l, r.level))
        )

      case AllOpen ⇒
        context become writing(i2c, R8.AllOpenState)
    }
  }

  def writing(i2c: Int, v: Int): Receive = {
    write(i2c, v).foreach(_ ⇒ self ! Done)

    {
      case Done ⇒
        unstashAll()
        context become initialized(i2c, v)

      case _ ⇒ stash()
    }
  }

  def init(): Future[Handle] = Future {
    Handle(R8.i2c(stack))
  }

  def write(i2c: Int, state: Int): Future[Int] = Future {
    lgpio.i2cWriteByteData(i2c, R8.RELAY8_OUTPORT_REG_ADD, state)
  }
}
