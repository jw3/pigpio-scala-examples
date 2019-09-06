pigpio-scala example
===

https://github.com/jw3/pigpio-scala

usage

`docker run --rm --privileged -u root -v /sys:/sys -v /dev/mem:/dev/mem pigpio-scala-examples`

### pigpio build

`make CFLAGS=-DEMBEDDED_IN_VM`

### references
- https://github.com/joan2937/pigpio/tree/master/EXAMPLES/CPP/ROTARY_ENCODER
- https://raspberrypi.stackexchange.com/a/62302
- https://github.com/pololu/vl53l1x-arduino
- https://github.com/arduino-libraries/Stepper/blob/master/src/Stepper.cpp
