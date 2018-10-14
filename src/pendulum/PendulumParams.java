package pendulum;

import com.pi4j.io.spi.SpiChannel;

import java.util.Locale;
import java.util.ResourceBundle;

public class PendulumParams {
    private int ledNum = 100;
    private int sizeX = 360;
    private int sizeY = 100;
    private int spiAPA102Speed = 1_000_000;
    private SpiChannel spiChannel = SpiChannel.CS0;
    private int displayFrequency = 300;

    public PendulumParams() {
        try {
            initParamFromResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initParamFromResources() {
        Locale locale = Locale.ENGLISH;
        ResourceBundle myResources = ResourceBundle.getBundle("resources",
                locale);
        int ledNumT = Integer.parseInt(myResources.getString("ledNum"));
        int sizeXT = Integer.parseInt(myResources.getString("sizeX"));
        int sizeYT = Integer.parseInt(myResources.getString("sizeY"));
        int spiAPA102SpeedT = Integer.parseInt(myResources.getString("spiAPA102Speed"));
        int displayFrequencyT = Integer.parseInt(myResources.getString("displayFrequency"));

        ledNum = ledNumT;
        sizeX = sizeXT;
        sizeY = sizeYT;
        spiAPA102Speed = spiAPA102SpeedT;
        displayFrequency = displayFrequencyT;
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
