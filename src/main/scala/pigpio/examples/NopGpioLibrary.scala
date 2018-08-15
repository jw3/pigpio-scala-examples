package pigpio.examples

import java.nio.{ByteBuffer, IntBuffer}

import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import pigpio.javadsl._
import pigpio.scaladsl.PigpioLibrary

object NopGpioLibrary extends PigpioLibrary {
  import PigpioLibrary._

  def gpioInitialise(): Int = PI_INITIALISED
  def gpioTerminate(): Unit = ()
  def gpioSetMode(gpio: Int, mode: Int): Int = 0
  def gpioGetMode(gpio: Int): Int = 0
  def gpioSetPullUpDown(gpio: Int, pud: Int): Int = 0
  def gpioRead(gpio: Int): Int = 0
  def gpioWrite(gpio: Int, level: Int): Int = 0
  def gpioPWM(user_gpio: Int, dutycycle: Int): Int = 0
  def gpioGetPWMdutycycle(user_gpio: Int): Int = 0
  def gpioSetPWMrange(user_gpio: Int, range: Int): Int = 0
  def gpioGetPWMrange(user_gpio: Int): Int = ???
  def gpioGetPWMrealRange(user_gpio: Int): Int = ???
  def gpioSetPWMfrequency(user_gpio: Int, frequency: Int): Int = 0
  def gpioGetPWMfrequency(user_gpio: Int): Int = 0
  def gpioServo(user_gpio: Int, pulsewidth: Int): Int = 0
  def gpioGetServoPulsewidth(user_gpio: Int): Int = 0
  def gpioSetAlertFunc(user_gpio: Int, f: PigpioLibrary.gpioAlertFunc_t): Int = 0
  def gpioSetAlertFuncEx(user_gpio: Int, f: PigpioLibrary.gpioAlertFuncEx_t, userdata: Pointer): Int = 0
  def gpioSetISRFunc(user_gpio: Int, edge: Int, timeout: Int, f: PigpioLibrary.gpioISRFunc_t): Int = 0
  def gpioSetISRFuncEx(user_gpio: Int,
                       edge: Int,
                       timeout: Int,
                       f: PigpioLibrary.gpioISRFuncEx_t,
                       userdata: Pointer): Int = 0
  def gpioNotifyOpen: Int = 0
  def gpioNotifyOpenWithSize(bufSize: Int): Int = 0
  def gpioNotifyBegin(handle: Int, bits: Int): Int = 0
  def gpioNotifyPause(handle: Int): Int = 0
  def gpioNotifyClose(handle: Int): Int = 0
  def gpioWaveClear: Int = 0
  def gpioWaveAddNew: Int = 0
  def gpioWaveAddGeneric(numPulses: Int, pulses: gpioPulse_t): Int = 0
  def gpioWaveAddSerial(user_gpio: Int,
                        baud: Int,
                        data_bits: Int,
                        stop_bits: Int,
                        offset: Int,
                        numBytes: Int,
                        str: Pointer): Int = 0
  def gpioWaveAddSerial(user_gpio: Int,
                        baud: Int,
                        data_bits: Int,
                        stop_bits: Int,
                        offset: Int,
                        numBytes: Int,
                        str: ByteBuffer): Int = 0
  def gpioWaveCreate: Int = 0
  def gpioWaveDelete(wave_id: Int): Int = 0
  def gpioWaveTxSend(wave_id: Int, wave_mode: Int): Int = 0
  def gpioWaveChain(buf: Pointer, bufSize: Int): Int = 0
  def gpioWaveChain(buf: ByteBuffer, bufSize: Int): Int = 0
  def gpioWaveTxAt: Int = 0
  def gpioWaveTxBusy: Int = 0
  def gpioWaveTxStop: Int = 0
  def gpioWaveGetMicros: Int = 0
  def gpioWaveGetHighMicros: Int = 0
  def gpioWaveGetMaxMicros: Int = 0
  def gpioWaveGetPulses: Int = 0
  def gpioWaveGetHighPulses: Int = 0
  def gpioWaveGetMaxPulses: Int = 0
  def gpioWaveGetCbs: Int = 0
  def gpioWaveGetHighCbs: Int = 0
  def gpioWaveGetMaxCbs: Int = 0
  def gpioSerialReadOpen(user_gpio: Int, baud: Int, data_bits: Int): Int = 0
  def gpioSerialReadInvert(user_gpio: Int, invert: Int): Int = 0
  def gpioSerialRead(user_gpio: Int, buf: Pointer, bufSize: Nothing): Int = 0
  def gpioSerialReadClose(user_gpio: Int): Int = 0
  def i2cOpen(i2cBus: Int, i2cAddr: Int, i2cFlags: Int): Int = 0
  def i2cClose(handle: Int): Int = 0
  def i2cWriteQuick(handle: Int, bit: Int): Int = 0
  def i2cWriteByte(handle: Int, bVal: Int): Int = 0
  def i2cReadByte(handle: Int): Int = 0
  def i2cWriteByteData(handle: Int, i2cReg: Int, bVal: Int): Int = 0
  def i2cWriteWordData(handle: Int, i2cReg: Int, wVal: Int): Int = 0
  def i2cReadByteData(handle: Int, i2cReg: Int): Int = 0
  def i2cReadWordData(handle: Int, i2cReg: Int): Int = 0
  def i2cProcessCall(handle: Int, i2cReg: Int, wVal: Int): Int = 0
  def i2cWriteBlockData(handle: Int, i2cReg: Int, buf: Pointer, count: Int): Int = 0
  def i2cWriteBlockData(handle: Int, i2cReg: Int, buf: ByteBuffer, count: Int): Int = 0
  def i2cReadBlockData(handle: Int, i2cReg: Int, buf: Pointer): Int = 0
  def i2cReadBlockData(handle: Int, i2cReg: Int, buf: ByteBuffer): Int = 0
  def i2cBlockProcessCall(handle: Int, i2cReg: Int, buf: Pointer, count: Int): Int = 0
  def i2cBlockProcessCall(handle: Int, i2cReg: Int, buf: ByteBuffer, count: Int): Int = 0
  def i2cReadI2CBlockData(handle: Int, i2cReg: Int, buf: Pointer, count: Int): Int = 0
  def i2cReadI2CBlockData(handle: Int, i2cReg: Int, buf: ByteBuffer, count: Int): Int = 0
  def i2cWriteI2CBlockData(handle: Int, i2cReg: Int, buf: Pointer, count: Int): Int = 0
  def i2cWriteI2CBlockData(handle: Int, i2cReg: Int, buf: ByteBuffer, count: Int): Int = 0
  def i2cReadDevice(handle: Int, buf: Pointer, count: Int): Int = 0
  def i2cReadDevice(handle: Int, buf: ByteBuffer, count: Int): Int = 0
  def i2cWriteDevice(handle: Int, buf: Pointer, count: Int): Int = 0
  def i2cWriteDevice(handle: Int, buf: ByteBuffer, count: Int): Int = 0
  def i2cSwitchCombined(setting: Int): Unit = ()
  def i2cSegments(handle: Int, segs: pi_i2c_msg_t, numSegs: Int): Int = 0
  def i2cZip(handle: Int, inBuf: Pointer, inLen: Int, outBuf: Pointer, outLen: Int): Int = 0
  def i2cZip(handle: Int, inBuf: ByteBuffer, inLen: Int, outBuf: ByteBuffer, outLen: Int): Int = 0
  def bbI2COpen(SDA: Int, SCL: Int, baud: Int): Int = 0
  def bbI2CClose(SDA: Int): Int = 0
  def bbI2CZip(SDA: Int, inBuf: Pointer, inLen: Int, outBuf: Pointer, outLen: Int): Int = 0
  def bbI2CZip(SDA: Int, inBuf: ByteBuffer, inLen: Int, outBuf: ByteBuffer, outLen: Int): Int = 0
  def spiOpen(spiChan: Int, baud: Int, spiFlags: Int): Int = 0
  def spiClose(handle: Int): Int = 0
  def spiRead(handle: Int, buf: Pointer, count: Int): Int = 0
  def spiRead(handle: Int, buf: ByteBuffer, count: Int): Int = 0
  def spiWrite(handle: Int, buf: Pointer, count: Int): Int = 0
  def spiWrite(handle: Int, buf: ByteBuffer, count: Int): Int = 0
  def spiXfer(handle: Int, txBuf: Pointer, rxBuf: Pointer, count: Int): Int = 0
  def spiXfer(handle: Int, txBuf: ByteBuffer, rxBuf: ByteBuffer, count: Int): Int = 0
  def serOpen(sertty: Pointer, baud: Int, serFlags: Int): Int = 0
  def serOpen(sertty: ByteBuffer, baud: Int, serFlags: Int): Int = 0
  def serClose(handle: Int): Int = 0
  def serWriteByte(handle: Int, bVal: Int): Int = 0
  def serReadByte(handle: Int): Int = 0
  def serWrite(handle: Int, buf: Pointer, count: Int): Int = 0
  def serWrite(handle: Int, buf: ByteBuffer, count: Int): Int = 0
  def serRead(handle: Int, buf: Pointer, count: Int): Int = 0
  def serRead(handle: Int, buf: ByteBuffer, count: Int): Int = 0
  def serDataAvailable(handle: Int): Int = 0
  def gpioTrigger(user_gpio: Int, pulseLen: Int, level: Int): Int = 0
  def gpioSetWatchdog(user_gpio: Int, timeout: Int): Int = 0
  def gpioNoiseFilter(user_gpio: Int, steady: Int, active: Int): Int = 0
  def gpioGlitchFilter(user_gpio: Int, steady: Int): Int = 0
  def gpioSetGetSamplesFunc(f: PigpioLibrary.gpioGetSamplesFunc_t, bits: Int): Int = 0
  def gpioSetGetSamplesFuncEx(f: PigpioLibrary.gpioGetSamplesFuncEx_t, bits: Int, userdata: Pointer): Int = 0
  def gpioSetTimerFunc(timer: Int, millis: Int, f: PigpioLibrary.gpioTimerFunc_t): Int = 0
  def gpioSetTimerFuncEx(timer: Int, millis: Int, f: PigpioLibrary.gpioTimerFuncEx_t, userdata: Pointer): Int = 0
  def gpioStartThread(f: PigpioLibrary.gpioThreadFunc_t, userdata: Pointer): PigpioLibrary.pthread_t = ???
  def gpioStopThread(pth: PigpioLibrary.pthread_t): Unit = ()
  def gpioStoreScript(script: Pointer): Int = 0
  def gpioStoreScript(script: ByteBuffer): Int = 0
  def gpioRunScript(script_id: Int, numPar: Int, param: IntByReference): Int = 0
  def gpioRunScript(script_id: Int, numPar: Int, param: IntBuffer): Int = 0
  def gpioScriptStatus(script_id: Int, param: IntByReference): Int = 0
  def gpioScriptStatus(script_id: Int, param: IntBuffer): Int = 0
  def gpioStopScript(script_id: Int): Int = 0
  def gpioDeleteScript(script_id: Int): Int = 0
  def gpioSetSignalFunc(signum: Int, f: PigpioLibrary.gpioSignalFunc_t): Int = 0
  def gpioSetSignalFuncEx(signum: Int, f: PigpioLibrary.gpioSignalFuncEx_t, userdata: Pointer): Int = 0
  def gpioRead_Bits_0_31: Int = 0
  def gpioRead_Bits_32_53: Int = 0
  def gpioWrite_Bits_0_31_Clear(bits: Int): Int = 0
  def gpioWrite_Bits_32_53_Clear(bits: Int): Int = 0
  def gpioWrite_Bits_0_31_Set(bits: Int): Int = 0
  def gpioWrite_Bits_32_53_Set(bits: Int): Int = 0
  def gpioHardwareClock(gpio: Int, clkfreq: Int): Int = 0
  def gpioHardwarePWM(gpio: Int, PWMfreq: Int, PWMduty: Int): Int = 0
  def gpioTime(timetype: Int, seconds: IntByReference, micros: IntByReference): Int = 0
  def gpioTime(timetype: Int, seconds: IntBuffer, micros: IntBuffer): Int = 0
  def gpioSleep(timetype: Int, seconds: Int, micros: Int): Int = 0
  def gpioDelay(micros: Int): Int = 0
  def gpioTick: Int = 0
  def gpioHardwareRevision: Int = 0
  def gpioVersion: Int = 0
  def gpioGetPad(var1: Int): Int = 0
  def gpioSetPad(var1: Int, var2: Int): Int = 0
  def eventMonitor(var1: Int, var2: Int): Int = 0
  def eventSetFunc(var1: Int, var2: PigpioLibrary.eventFunc_t): Int = 0
  def eventSetFuncEx(var1: Int, var2: PigpioLibrary.eventFuncEx_t, var3: Pointer): Int = 0
  def eventTrigger(var1: Int): Int = 0
  def shell(var1: Pointer, var2: Pointer): Int = 0
  def shell(var1: ByteBuffer, var2: ByteBuffer): Int = 0
  def fileOpen(var1: Pointer, var2: Int): Int = 0
  def fileOpen(var1: ByteBuffer, var2: Int): Int = 0
  def fileClose(var1: Int): Int = 0
  def fileWrite(var1: Int, var2: Pointer, var3: Int): Int = 0
  def fileWrite(var1: Int, var2: ByteBuffer, var3: Int): Int = 0
  def fileRead(var1: Int, var2: Pointer, var3: Int): Int = 0
  def fileRead(var1: Int, var2: ByteBuffer, var3: Int): Int = 0
  def fileSeek(var1: Int, var2: Int, var3: Int): Int = 0
  def fileList(var1: Pointer, var2: Pointer, var3: Int): Int = 0
  def fileList(var1: ByteBuffer, var2: ByteBuffer, var3: Int): Int = 0
  def gpioCfgBufferSize(cfgMillis: Int): Int = 0
  def gpioCfgClock(cfgMicros: Int, cfgPeripheral: Int, cfgSource: Int): Int = 0
  def gpioCfgDMAchannel(DMAchannel: Int): Int = 0
  def gpioCfgDMAchannels(primaryChannel: Int, secondaryChannel: Int): Int = 0
  def gpioCfgPermissions(updateMask: Long): Int = 0
  def gpioCfgSocketPort(port: Int): Int = 0
  def gpioCfgInterfaces(ifFlags: Int): Int = 0
  def gpioCfgMemAlloc(memAllocMode: Int): Int = 0
  def gpioCfgInternals(cfgWhat: Int, cfgVal: Int): Int = 0
  def gpioCfgGetInternals: Int = 0
  def gpioCfgSetInternals(cfgVal: Int): Int = 0
  def gpioCustom1(arg1: Int, arg2: Int, argx: Pointer, argc: Int): Int = 0
  def gpioCustom1(arg1: Int, arg2: Int, argx: ByteBuffer, argc: Int): Int = 0
  def gpioCustom2(arg1: Int, argx: Pointer, argc: Int, retBuf: Pointer, retMax: Int): Int = 0
  def gpioCustom2(arg1: Int, argx: ByteBuffer, argc: Int, retBuf: ByteBuffer, retMax: Int): Int = 0
  def rawWaveAddSPI(spi: rawSPI_t,
                    offset: Int,
                    spiSS: Int,
                    buf: Pointer,
                    spiTxBits: Int,
                    spiBitFirst: Int,
                    spiBitLast: Int,
                    spiBits: Int): Int = 0
  def rawWaveAddSPI(spi: rawSPI_t,
                    offset: Int,
                    spiSS: Int,
                    buf: ByteBuffer,
                    spiTxBits: Int,
                    spiBitFirst: Int,
                    spiBitLast: Int,
                    spiBits: Int): Int = 0
  def rawWaveAddGeneric(numPulses: Int, pulses: rawWave_t): Int = 0
  def rawWaveCB: Int = 0
  def rawWaveCBAdr(cbNum: Int): rawCbs_t = ???
  def rawWaveGetOOL(var1: Int): Int = 0
  def rawWaveSetOOL(var1: Int, var2: Int): Unit = ()
  def rawWaveGetOut(pos: Int): Int = 0
  def rawWaveSetOut(pos: Int, lVal: Int): Unit = ()
  def rawWaveGetIn(pos: Int): Int = 0
  def rawWaveSetIn(pos: Int, lVal: Int): Unit = ()
  def rawWaveInfo(wave_id: Int): rawWaveInfo_t.ByValue = ???
  def getBitInBytes(bitPos: Int, buf: Pointer, numBits: Int): Int = 0
  def getBitInBytes(bitPos: Int, buf: ByteBuffer, numBits: Int): Int = 0
  def putBitInBytes(bitPos: Int, buf: Pointer, bit: Int): Unit = ()
  def putBitInBytes(bitPos: Int, buf: ByteBuffer, bit: Int): Unit = ()
  def time_time: Double = 0.0
  def time_sleep(seconds: Double): Unit = ()
  def rawDumpWave(): Unit = ()
  def rawDumpScript(script_id: Int): Unit = ()
}
