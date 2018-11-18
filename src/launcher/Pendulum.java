package launcher;

import AHRS.AHRS;
import UsbReader.FlashReader;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import devices.Protocol.spi.Pi4SPIDevice;
import devices.sensorImplementations.MPU9250.MPU9250;
import pendulum.Loader.HDDLoader;
import pendulum.PendulumParams;
import pendulum.display.ImgDisplay;
import pendulum.display.impl.ImgDefaultDisplayImpl;
import pendulum.display.impl.ImgFirstDisplayImpl;
import pendulum.stateMachine.Impl.PendulumStateMachineImpl;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgListStorage;
import pendulum.storage.Impl.ImgListStorageImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


public class Pendulum {

    public static void main(String[] args) throws IOException, InterruptedException {

        PendulumParams params = PendulumParams.getInstance();
        ImgListStorage imgStorage = new ImgListStorageImpl(params.getSizeX(), params.getSizeY());
        List<ImgDisplay> imgDisplayList = new ArrayList<>();
        HDDLoader hddLoader = new HDDLoader(imgStorage);
        FlashReader flashReader = new FlashReader(hddLoader);

        new Timer().schedule(flashReader, 0, 1000);
        hddLoader.Load();

        ImgDisplay imgDisplay = new ImgDefaultDisplayImpl(params.getSpiApa102Channel(), params.getSpiAPA102Speed(), params.getSizeX(), params.getLedNum());
        imgDisplayList.add(imgDisplay);

        PendulumStateMachine stateMachine = new PendulumStateMachineImpl(imgDisplayList, imgStorage, params.getSizeX(), params.getSizeY());

        MPU9250 mpu9250 = new MPU9250(
                new Pi4SPIDevice(SpiFactory.getInstance(params.getSpiSensorChannel(),
                        params.getSpiAPA102Speed(),
                        SpiMode.MODE_0)),
                params.getDisplayFrequency());
        AHRS ahrs = new AHRS(mpu9250);
        ahrs.setGyroOffset();

        while (true) {
            ahrs.imuLoop();
            stateMachine.readNewSample(ahrs.getQ());
            Thread.sleep(1000 / params.getDisplayFrequency());
        }
    }
}
