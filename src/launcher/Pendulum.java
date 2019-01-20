package launcher;

import AHRS.AHRS;
import observer.EventListener;
import observer.EventManager;
import observer.EventType;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import devices.Protocol.ProtocolInterface;
import devices.Protocol.i2c.Pi4jI2CDevice;
import devices.sensorImplementations.MPU9250.MPU9250;
import devices.sensors.NineDOF;
import pendulum.PendulumParams;
import pendulum.StorageFileGetter;
import pendulum.display.ImgDisplay;
import pendulum.display.impl.ImgDefaultDisplayImpl;
import pendulum.stateMachine.Impl.PendulumStateMachineImpl;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.ImgListStorage;
import pendulum.storage.Impl.ImgListStorageImpl;
import transmission.device.Device;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;


public class Pendulum implements Runnable {
    private static EventManager eventManager;
    private static Device device = Device.getInstance();
    private static File storage;

    public Pendulum(EventManager eventManager) {
        Pendulum.eventManager = eventManager;
        storage = new StorageFileGetter().getStorage();
    }

    @Override
    public void run() {
        try {
            device.setStorage(storage);
            device.loadFromStorage();
            PendulumParams params = PendulumParams.getInstance();
            ImgListStorage imgStorage = new ImgListStorageImpl(params.getPolarYSize());

            ImgDisplay imgDisplay = new ImgDefaultDisplayImpl(params.getSpiApa102Channel(), params.getSpiAPA102Speed());

            PendulumStateMachine stateMachine = new PendulumStateMachineImpl(imgDisplay, imgStorage);

            eventManager.subscribe(EventType.STORAGE_UPDATED, (EventListener) stateMachine);
            eventManager.subscribe(EventType.MESSAGE_RECEIVE, (EventListener) stateMachine);

            I2CBus bus = I2CFactory.getInstance(params.getI2cBus());
            ProtocolInterface protocolInterfaceI2C = new Pi4jI2CDevice(bus.getDevice(0x68));
//            ProtocolInterface protocolInterfaceSPI = new Pi4jSPIDevice(SpiFactory.getInstance(
//                    params.getSpiSensorChannel(),
//                    params.getSpiAPA102Speed(),
//                    SpiMode.MODE_0));

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
//            System.out.println(counter + " counter");
        } catch (Exception e) {
                throw new RuntimeException(e);
        }
    }
}
