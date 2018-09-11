package pigpio.examples

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import pigpio.examples.Stepper.{
  NextStep,
  StartStepping,
  StepDirection,
  StepSize
}
import pigpio.scaladsl._

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

object Stepper6612 extends App {
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

  val gpio = Seq(4, 5, 6, 7).map(UserGpio(_))
  println(s".:| Example of stepping with a TB6612 on pins $gpio |:.")

  system.actorOf(Stepper.props(Stepper.HalfStep, gpio))
}

object Stepper {
  def props(size: StepSize, pins: Seq[UserGpio])(
      implicit lgpio: PigpioLibrary) =
    Props(new Stepper(size, pins))

  sealed trait StepSize {
    def steps: Seq[Int]
    def size: Int = steps.size
    def next(prev: Int, dir: StepDirection): Int = dir match {
      case Forward ⇒ if (prev < size - 1) prev + 1 else 0
      case Reverse ⇒ if (prev > 0) prev - 1 else size - 1
    }
  }
  object FullStep extends StepSize { val steps = Seq(8, 4, 2, 1) }
  object HalfStep extends StepSize { val steps = Seq(8, 12, 4, 6, 2, 3, 1, 9) }

  def levels(v: Int): Seq[Level] = Seq(
    Level(v & 1 << 3),
    Level(v & 1 << 2),
    Level(v & 1 << 1),
    Level(v & 1)
  )

  sealed trait StepDirection
  object Forward extends StepDirection
  object Reverse extends StepDirection

  case class StartStepping(dir: StepDirection, delay: FiniteDuration)
  case object NextStep
}

class Stepper(size: StepSize, pins: Seq[UserGpio])(
    implicit lgpio: PigpioLibrary)
    extends Actor
    with Timers
    with ActorLogging {

  val gpio: Seq[ActorRef] = pins.map(p ⇒ context.actorOf(GpioPin.props(p)))
  gpio.foreach(_ ! OutputPin)

  def ready: Receive = {
    case StartStepping(dir, delay) ⇒
      gpio.zip(Stepper.levels(size.steps.head)).foreach(x ⇒ x._1 ! x._2)
      context.become(stepping(size.next(0, dir), dir, delay))
  }

  def stepping(step: Int,
               dir: StepDirection,
               delay: FiniteDuration): Receive = {

    timers.startSingleTimer("step", NextStep, delay)

    {
      case NextStep ⇒
        gpio.zip(Stepper.levels(size.steps.head)).foreach(x ⇒ x._1 ! x._2)
        context.become(stepping(size.next(step, dir), dir, delay))
    }
  }

  def receive: Receive = ready
}
