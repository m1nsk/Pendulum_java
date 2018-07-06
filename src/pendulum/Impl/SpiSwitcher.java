package pendulum.Impl;

import com.pi4j.io.gpio.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SpiSwitcher {

    private static final GpioController gpio;

    // provision gpio pin #01 as an output pin and turn on
    private static GpioPinDigitalOutput APAFirstPin;
    private static GpioPinDigitalOutput APASecondPin;
    private static GpioPinDigitalOutput MPU9250Pin;
    private static Lock lock;

    static {
        gpio = GpioFactory.getInstance();
        APAFirstPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
        APASecondPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.LOW);
        MPU9250Pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
        lock = new ReentrantLock();
    }

    public static void setAPAFirst() {
        lock.lock();
        APAFirstPin.high();
    }

    public static void setAPASecond() {
        lock.lock();
        APASecondPin.high();
    }

    public static void setMPU9250() {
        lock.lock();
        MPU9250Pin.high();
    }

    public static void setNone() {
        APAFirstPin.low();
        APASecondPin.low();
        MPU9250Pin.low();
        lock.unlock();
    }
}
