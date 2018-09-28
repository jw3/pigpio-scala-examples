package pigpio.examples

import java.nio.ByteBuffer

import akka.actor.ActorSystem
import pigpio.scaladsl.PigpioLibrary
import pigpio.vl53l1x.javadsl.{I2C_HandleTypeDef, VL53L1_Dev_t, VL53L1_RangingMeasurementData_t, Vl53l1xLibrary}

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

  val vl53l1 = Vl53l1xLibrary.INSTANCE

  val i2c = lgpio.i2cOpen(1, 0x29, 0)
  val Dev = {
    val dev = new VL53L1_Dev_t
    dev.I2cHandle = new I2C_HandleTypeDef.ByReference
    dev.I2cHandle.dummy = i2c
    dev
  }

  vl53l1.VL53L1_software_reset(Dev)

  val bb = ByteBuffer.allocate(16)

  val res = vl53l1.VL53L1_RdByte(
    Dev,
    Vl53l1xLibrary.VL53L1_IDENTIFICATION__MODEL_ID.toShort,
    bb)

  println(s"VL53L1_IDENTIFICATION__MODEL_ID res $res")

  val model = bb.asIntBuffer().get().toHexString
  println(s"VL53L1X Model_ID: $model")

  bb.clear()
  val x: Byte = vl53l1.VL53L1_WaitDeviceBooted(Dev)

  println(vl53l1.VL53L1_WaitDeviceBooted(Dev))
  println(vl53l1.VL53L1_DataInit(Dev))
  println(vl53l1.VL53L1_StaticInit(Dev))
  println(vl53l1.VL53L1_SetPresetMode(Dev, 4)) // VL53L1_PRESETMODE_LITE_RANGING
  println(vl53l1.VL53L1_SetDistanceMode(Dev, 1)) //
  println(vl53l1.VL53L1_SetMeasurementTimingBudgetMicroSeconds(Dev, 20000))
  println(vl53l1.VL53L1_SetInterMeasurementPeriodMilliSeconds(Dev, 55))
  println(vl53l1.VL53L1_StartMeasurement(Dev))

  for (i ← 1 to 100) {
    if (0 == vl53l1.VL53L1_WaitMeasurementDataReady(Dev)) {
      val d = new VL53L1_RangingMeasurementData_t
      if (0 == vl53l1.VL53L1_GetRangingMeasurementData(Dev, d)) {
        println(s"Range ${d.RangeMilliMeter} mm")
      }
      vl53l1.VL53L1_ClearInterruptAndStartMeasurement(Dev)
    }
  }
}
