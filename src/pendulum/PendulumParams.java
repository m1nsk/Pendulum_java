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
        Locale locale = Locale.ENGLISH;
        ResourceBundle myResources = ResourceBundle.getBundle("MyResources",
                locale);
        String string = myResources.getString("HelpKey");
        System.out.println("HelpKey: " + string);
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
