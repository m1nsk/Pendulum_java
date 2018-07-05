package pendulum;

import com.pi4j.io.spi.SpiChannel;

public class PendulumParams {
    private int ledNum = 100;
    private int sizeX = 360;
    private int sizeY = 100;
    private int spiAPA102Speed = 1_000_000;
    private SpiChannel spiChannel = SpiChannel.CS0;
    private int displayFrequency = 300;

    public PendulumParams() {
    }

    public int getLedNum() {
        return ledNum;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSpiAPA102Speed() {
        return spiAPA102Speed;
    }

    public SpiChannel getSpiChannel() {
        return spiChannel;
    }

    public int getDisplayFrequency() {
        return displayFrequency;
    }
}
