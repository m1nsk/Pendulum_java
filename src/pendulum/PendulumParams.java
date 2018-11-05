package pendulum;

import com.pi4j.io.spi.SpiChannel;

import java.util.Locale;
import java.util.ResourceBundle;

public class PendulumParams {
    private Integer ledNum = 100;
    private Integer sizeX = 360;
    private Integer sizeY = 100;
    private Integer spiAPA102Speed = 1_000_000;
    private Integer spiSensorSpeed = 1_000_000;
    private SpiChannel spiSensorChannel = SpiChannel.CS0;
    private SpiChannel spiApa102Channel = SpiChannel.CS1;
    private Integer displayFrequency = 300;
    private String imageFolderPath = "images/test.txt";

    public PendulumParams() {
        initParamFromResources();
    }

    private String tryGetResource(ResourceBundle resourceBundle, String value) {
        try {
            return resourceBundle.getString(value);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private void initParamFromResources() {
        Locale locale = Locale.ENGLISH;
        ResourceBundle myResources = ResourceBundle.getBundle("resources", locale);
        if (myResources != null) {
            setLedNum(tryParseInt(tryGetResource(myResources, "ledNum")));
            setSizeX(tryParseInt(tryGetResource(myResources, "sizeX")));
            setSizeY(tryParseInt(tryGetResource(myResources, "sizeY")));
            setSpiAPA102Speed(tryParseInt(tryGetResource(myResources, "spiAPA102Speed")));
            setSpiSensorSpeed(tryParseInt(tryGetResource(myResources, "spiSensorSpeed")));
            setDisplayFrequency(tryParseInt(tryGetResource(myResources, "displayFrequency")));
        }
    }

    public void setLedNum(Integer ledNum) {
        if (ledNum != null)
            this.ledNum = ledNum;
    }

    public void setSizeX(Integer sizeX) {
        if (sizeX != null)
            this.sizeX = sizeX;
    }

    public void setSizeY(Integer sizeY) {
        if(sizeY != null)
            this.sizeY = sizeY;
    }

    public void setSpiAPA102Speed(Integer spiAPA102Speed) {
        if(spiAPA102Speed != null)
            this.spiAPA102Speed = spiAPA102Speed;
    }

    public void setSpiSensorSpeed(Integer spiSensorSpeed) {
        if (spiSensorSpeed != null)
            this.spiSensorSpeed = spiSensorSpeed;
    }

    public void setSpiSensorChannel(SpiChannel spiSensorChannel) {
        if (spiSensorChannel != null)
            this.spiSensorChannel = spiSensorChannel;
    }

    public void setSpiApa102Channel(SpiChannel spiApa102Channel) {
        if (spiApa102Channel != null)
            this.spiApa102Channel = spiApa102Channel;
    }

    public void setDisplayFrequency(Integer displayFrequency) {
        if(displayFrequency != null)
            this.displayFrequency = displayFrequency;
    }

    public Integer getLedNum() {
        return ledNum;
    }

    public Integer getSizeX() {
        return sizeX;
    }

    public Integer getSizeY() {
        return sizeY;
    }

    public Integer getSpiAPA102Speed() {
        return spiAPA102Speed;
    }

    public Integer getSpiSensorSpeed() {
        return spiSensorSpeed;
    }

    public SpiChannel getSpiSensorChannel() {
        return spiSensorChannel;
    }

    public SpiChannel getSpiApa102Channel() {
        return spiApa102Channel;
    }

    public Integer getDisplayFrequency() {
        return displayFrequency;
    }

    public String getImageFolderPath() {
        return imageFolderPath;
    }

    public void setImageFolderPath(String imageFolderPath) {
        this.imageFolderPath = imageFolderPath;
    }
}
