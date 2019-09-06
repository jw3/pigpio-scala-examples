package pigpio.examples

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import pigpio.examples.GpioPinGroup.{MemberLevels, MemberPinMode}
import pigpio.examples.Stepper._
import pigpio.scaladsl._

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext}

object Stepper6612 extends App {
  implicit val system: ActorSystem = ActorSystem("pigpio-example")
  implicit val lgpio: PigpioLibrary = PigpioLibrary.INSTANCE

  val delay = args.headOption match {
    case Some(v) ⇒ v.toInt.millis
    case None ⇒ 1.micros
  }

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

  val (a2, a1, b1, b2) = (17, 18, 22, 23)
  val gpio = Seq(a2, a1, b1, b2).map(UserGpio)

  println(s".:| Example of stepping with a TB6612 on pins a2=$a2 a1=$a1 b1=$b1 b2=$b2 |:.")

  val stepper = system.actorOf(Stepper.props(Stepper.FullStep, gpio))
  stepper ! StartStepping(Forward, delay)

  import ExecutionContext.Implicits.global
  Await.ready(system.whenTerminated, Duration.Inf).andThen{
    case _ ⇒ lgpio.gpioTerminate()
  }
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

  // A+ A- B+ B-
  // ===========
  // A+B+, A-B+, A-B-, A+B-
  object FullStep extends StepSize { val steps = Seq(10, 6, 5, 9) }
  // A+B+, A+, A+B-, B-, A-B-, A-, A-B+, B+
  object HalfStep extends StepSize { val steps = Seq(10, 2, 6, 4, 5, 1, 9, 8) }

  def levels(v: Int): Seq[Level] = v.toBinaryString.reverse.padTo(4, '0').reverse.map {
    case '0' ⇒ Low
    case '1' ⇒ High
    case _ ⇒ throw BadLevel()
  }

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

  val gpio: ActorRef = context.actorOf(GpioPinGroup.props(pins: _*))
  gpio ! MemberPinMode(OutputPin, pins)

  def ready: Receive = {
    case StartStepping(dir, delay) ⇒
      context.become(stepping(0, dir, delay))
  }

  def stepping(step: Int,
               dir: StepDirection,
               delay: FiniteDuration): Receive = {

    timers.startSingleTimer("step", NextStep, delay)

    {
      case NextStep ⇒
        gpio ! levels(step)
        context.become(stepping(size.next(step, dir), dir, delay))
    }
  }

  def levels(step: Int): MemberLevels = {
    val x = pins.zip(Stepper.levels(size.steps(step)))
    GpioPinGroup.MemberLevels(x)
  }

  def receive: Receive = ready
}
