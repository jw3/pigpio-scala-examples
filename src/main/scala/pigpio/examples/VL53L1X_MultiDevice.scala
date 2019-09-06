package pigpio.examples

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash, Timers}
import pigpio.examples.Dev.{BootDevice, _}
import pigpio.scaladsl.GpioPin.Listen
import pigpio.scaladsl._
import pigpio.vl53l1x.javadsl.Vl53l1xLibrary.{INSTANCE ⇒ vl53l1}
import pigpio.vl53l1x.javadsl.{I2C_HandleTypeDef, VL53L1_Dev_t, VL53L1_RangingMeasurementData_t}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

object VL53L1X_MultiDevice extends App {
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

  println(s".:| Example of initializing multiple vl53l1 sensors on i2c |:.")

  val addr: Byte = 0x29.toByte
  val addr1: Byte = 0x2a.toByte
  val addr2: Byte = 0x2b.toByte

  val xshut1 = UserGpio(22)
  val xshut2 = UserGpio(24)
  val interrupt1 = UserGpio(23)
  val interrupt2 = UserGpio(25)

  val dev1 = system.actorOf(Dev.props(interrupt1, xshut1), "dev1")
  val dev2 = system.actorOf(Dev.props(interrupt2, xshut2), "dev2")

  dev1 ! BootDevice(addr1)
  dev1 ! Configure(20 * 1000, 55)

  Thread.sleep(5000)

  dev2 ! BootDevice(addr2)
  dev2 ! Configure(20 * 1000, 55)
}

object Dev {
  def props(int: UserGpio, xshut: UserGpio)(implicit lgpio: PigpioLibrary) = Props(new Dev(int, xshut))

  case class Configure(tb: Int, tp: Int)

  case object Sample

  case class Range(mm: Int)

  case class BootDevice(addr: Byte = 0x29)

  private case object DeviceBooted

  def Dev_t(addr: Int): VL53L1_Dev_t = {
    val dev = new VL53L1_Dev_t
    dev.I2cHandle = new I2C_HandleTypeDef.ByReference
    dev.I2cHandle.dummy = addr
    dev
  }

  val DefaultAddr: Byte = 0x29
}

class Dev(int: UserGpio, xshut: UserGpio)(implicit lgpio: PigpioLibrary) extends Actor with Stash with Timers with ActorLogging {
  val interruptPin: ActorRef = context.actorOf(GpioPin.props(int))
  val xshutPin: ActorRef = context.actorOf(GpioPin.props(xshut))

  xshutPin ! OutputPin
  interruptPin ! Listen()
  interruptPin ! InputPin

  def suspended: Receive = {
    xshutPin ! Low

    {
      case BootDevice(DefaultAddr) ⇒
        xshutPin ! High
        Thread.sleep(100) // no

        val i2c = lgpio.i2cOpen(1, DefaultAddr, 0)
        context.become(booting(Dev.Dev_t(i2c)))

      case BootDevice(addr) ⇒
        xshutPin ! High
        Thread.sleep(100) // no

      {
        val i2c = lgpio.i2cOpen(1, DefaultAddr, 0)
        val addr2x = addr + addr
        vl53l1.VL53L1_SetDeviceAddress(Dev.Dev_t(i2c), addr2x.byteValue())
        log.info("set address to {}", addr.toHexString)
        lgpio.i2cClose(i2c)
      }

        val i2c = lgpio.i2cOpen(1, addr, 0)
        context.become(booting(Dev.Dev_t(i2c)))
    }
  }

  def booting(dev: VL53L1_Dev_t): Receive = {
    import context.dispatcher

    log.info("waiting on device to boot")
    Future {
      vl53l1.VL53L1_WaitDeviceBooted(dev)
      context.self ! DeviceBooted
    }

    {
      case DeviceBooted ⇒
        unstashAll()
        log.info("device has booted")
        context.become(booted(dev))

      case _ ⇒ stash()
    }
  }

  def booted(dev: VL53L1_Dev_t): Receive = {
    case Configure(tb, tp) ⇒
      log.info("configuring device at {}:{}", tb, tp)

      println(vl53l1.VL53L1_DataInit(dev))
      println(vl53l1.VL53L1_StaticInit(dev))
      println(vl53l1.VL53L1_SetPresetMode(dev, 4)) // VL53L1_PRESETMODE_LITE_RANGING
      println(vl53l1.VL53L1_SetDistanceMode(dev, 1)) // VL53L1_DISTANCEMODE_SHORT
      println(vl53l1.VL53L1_SetMeasurementTimingBudgetMicroSeconds(dev, tb))
      println(vl53l1.VL53L1_SetInterMeasurementPeriodMilliSeconds(dev, tp))
      println(vl53l1.VL53L1_StartMeasurement(dev))

      context.become(configured(dev))
  }

  def configured(dev: VL53L1_Dev_t): Receive = {
    log.info("ready to measure")
    timers.startSingleTimer("sample", Sample, 1.second)

    {
      case Sample ⇒
        log.debug("starting measurement")
        vl53l1.VL53L1_ClearInterruptAndStartMeasurement(dev)

        if (0 == vl53l1.VL53L1_WaitMeasurementDataReady(dev)) {
          val d = new VL53L1_RangingMeasurementData_t
          if (0 == vl53l1.VL53L1_GetRangingMeasurementData(dev, d))
            log.info("Range {} mm", d.RangeMilliMeter)
          else
            log.warning("measurement failed")
        }

        timers.startSingleTimer("sample", Sample, 1.second)
    }
  }

  def receive: Receive = suspended
}
