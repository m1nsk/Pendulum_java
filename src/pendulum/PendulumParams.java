package pendulum;

import com.pi4j.io.spi.SpiChannel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


@Getter
@Setter
public class PendulumParams {
    private static volatile PendulumParams instance;
    private Integer ledNum = 100;
    private Integer sizeX = 360;
    private Integer sizeY = 100;
    private Integer spiAPA102Speed = 1_000_000;
    private Integer spiSensorSpeed = 1_000_000;
    private SpiChannel spiSensorChannel = SpiChannel.CS0;
    private SpiChannel spiApa102Channel = SpiChannel.CS1;
    private Integer displayFrequency = 300;
    private static final String PATH_TO_STORAGE = "storage/";
    private ClassLoader classLoader = getClass().getClassLoader();
    private String storagePath = new File(classLoader.getResource(PATH_TO_STORAGE).getFile()).getPath();

    private PendulumParams() {
        initParamFromResources();
    }

    public static PendulumParams getInstance() {
        PendulumParams localInstance = instance;
        if (localInstance == null) {
            synchronized (PendulumParams.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new PendulumParams();
                }
            }
        }
        return localInstance;
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

    public void initParamFromResources() {
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
        return new File(storagePath + "/instructions");
    }

    public File getImagesStorageFolder() {
        return new File(storagePath + "/images");
    }

    public File getConfigStorageFolder() {
        return new File(storagePath + "/config.properties");
    }

    public void updateParams(Properties newProperties) throws IOException {
        Properties properties = new Properties();
        try(FileInputStream fis = new FileInputStream(getConfigStorageFolder())){
            properties.load(new FileInputStream(getConfigStorageFolder()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(getConfigStorageFolder())) {
            newProperties.forEach((key, value) -> properties.setProperty((String) key, (String) value));
            properties.store(fos, "");
        }
        initParamFromResources();
    }
}
