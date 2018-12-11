package launcher;

import AHRS.AHRS;
import Observer.EventListener;
import Observer.EventManager;
import Observer.EventType;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import devices.Protocol.ProtocolInterface;
import devices.Protocol.i2c.Pi4jI2CDevice;
import devices.Protocol.spi.Pi4jSPIDevice;
import devices.sensorImplementations.MPU9250.MPU9250;
import devices.sensors.NineDOF;
import pendulum.PendulumParams;
import pendulum.display.ImgDisplay;
import pendulum.display.impl.ImgDefaultDisplayImpl;
import pendulum.stateMachine.Impl.PendulumStateMachineImpl;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgListStorage;
import pendulum.storage.Impl.ImgListStorageImpl;
import transmission.device.Device;

import java.io.File;
import java.util.Collections;


public class Pendulum implements Runnable {
    private static EventManager eventManager;
    private static final String PATH_TO_STORAGE = "storage/";
    private static ClassLoader classLoader;
    private static Device device = Device.getInstance();
    private static File storage;

    public Pendulum(EventManager eventManager) {
        Pendulum.eventManager = eventManager;
        classLoader = this.getClass().getClassLoader();
    }

    @Override
    public void run() {
        try {
            storage = new File(classLoader.getResource(PATH_TO_STORAGE).getFile());
            device.setStorage(storage);
            device.loadFromStorage();
            Integer ledNum = device.getLedNum();
            Integer ySize = ledNum;
            Integer sizeX = ledNum * 2;
            PendulumParams params = PendulumParams.getInstance();
            ImgListStorage imgStorage = new ImgListStorageImpl(sizeX, ySize, params.getPolarYSize());

            ImgDisplay imgDisplay = new ImgDefaultDisplayImpl(params.getSpiApa102Channel(), params.getSpiAPA102Speed(), params.getPolarYSize());

            PendulumStateMachine stateMachine = new PendulumStateMachineImpl(Collections.singletonList(imgDisplay), imgStorage);

            eventManager.subscribe(EventType.STORAGE_UPDATED, (EventListener) stateMachine);

            I2CBus bus = I2CFactory.getInstance(params.getI2cBus());
            ProtocolInterface protocolInterfaceI2C = new Pi4jI2CDevice(bus.getDevice(0x68));
            ProtocolInterface protocolInterfaceSPI = new Pi4jSPIDevice(SpiFactory.getInstance(
                    params.getSpiSensorChannel(),
                    params.getSpiAPA102Speed(),
                    SpiMode.MODE_0));

            NineDOF mpu9250 = new MPU9250(
                    protocolInterfaceI2C,
                    params.getDisplayFrequency());
            AHRS ahrs = new AHRS(mpu9250);
            ahrs.setGyroOffset();

            while (true) {
                ahrs.imuLoop();
                stateMachine.readNewSample(ahrs.getQ());
                Thread.sleep(1000 / params.getDisplayFrequency());
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
