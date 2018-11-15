package pendulum;

import com.pi4j.io.gpio.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SpiSwitcher {

    private static final GpioController gpio;

    // provision gpio pin #01 as an output pin and turn on
    private static GpioPinDigitalOutput APAFirstPin;

    static {
        gpio = GpioFactory.getInstance();
        APAFirstPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW);
    }

    public static void setAPAFirst() {
        APAFirstPin.high();
    }

    public static void setNone() {
        APAFirstPin.low();
    }
}
