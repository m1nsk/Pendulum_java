package pendulum;

import com.pi4j.io.spi.SpiChannel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

@Getter
@Setter
public class PendulumParams {
    private Integer ledNum = 100;
    private Integer sizeX = 360;
    private Integer sizeY = 100;
    private Integer spiAPA102Speed = 1_000_000;
    private Integer spiSensorSpeed = 1_000_000;
    private SpiChannel spiSensorChannel = SpiChannel.CS0;
    private SpiChannel spiApa102Channel = SpiChannel.CS1;
    private Integer displayFrequency = 300;
    private static final String PATH_TO_STORAGE = "storage/";

    public PendulumParams() {
        initParamFromResources();
    }

    private String tryGetResource(Properties properties, String value) {
        try {
            return properties.getProperty(value);
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
        Properties myResources = new Properties();
        try {
            myResources.load(new FileInputStream(getConfigStorageFolder()));
            setLedNum(tryParseInt(tryGetResource(myResources, "ledNum")));
            setSizeX(tryParseInt(tryGetResource(myResources, "sizeX")));
            setSizeY(tryParseInt(tryGetResource(myResources, "sizeY")));
            setSpiAPA102Speed(tryParseInt(tryGetResource(myResources, "spiAPA102Speed")));
            setSpiSensorSpeed(tryParseInt(tryGetResource(myResources, "spiSensorSpeed")));
            setDisplayFrequency(tryParseInt(tryGetResource(myResources, "displayFrequency")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLedNum(Integer ledNum) {
        if (ledNum != null)
            this.ledNum = ledNum;
    }

    private void setSizeX(Integer sizeX) {
        if (sizeX != null)
            this.sizeX = sizeX;
    }

    private void setSizeY(Integer sizeY) {
        if(sizeY != null)
            this.sizeY = sizeY;
    }

    private void setSpiAPA102Speed(Integer spiAPA102Speed) {
        if(spiAPA102Speed != null)
            this.spiAPA102Speed = spiAPA102Speed;
    }

    private void setSpiSensorSpeed(Integer spiSensorSpeed) {
        if (spiSensorSpeed != null)
            this.spiSensorSpeed = spiSensorSpeed;
    }

    public File getInstructionsStorageFolder() {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(PATH_TO_STORAGE + "instructions").getFile());
    }

    public File getImagesStorageFolder() {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(PATH_TO_STORAGE + "image").getFile());
    }

    public File getConfigStorageFolder() {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(PATH_TO_STORAGE + "config").getFile());
    }

    public void updateParams(Properties newProperties) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(getConfigStorageFolder()));
        newProperties.forEach((key, value) -> properties.setProperty((String)key, (String)value));
        initParamFromResources();
    }
}
