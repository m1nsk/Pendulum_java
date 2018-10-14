package launcher;

import AHRS.AHRS;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import devices.Protocol.spi.Pi4SPIDeviceSpiSelect;
import devices.sensorImplementations.MPU9250.MPU9250;
import pendulum.PendulumParams;
import pendulum.display.ImgDisplay;
import pendulum.display.impl.ImgFirstDisplayImpl;
import pendulum.display.impl.ImgSecondDisplayImpl;
import pendulum.stateMachine.Impl.PendulumStateMachineImpl;
import pendulum.storage.ImgStorage;
import pendulum.stateMachine.PendulumStateMachine;
import pendulum.storage.Impl.ImgStorageImpl;
import server.ServerPendulum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Pendulum {

    private static PendulumParams params = new PendulumParams();
    private static ImgStorage imgStorage = new ImgStorageImpl(params.getSizeX(), params.getSizeY());
    private static List<ImgDisplay> imgDisplayList = new ArrayList<>();
    private static PendulumStateMachine stateMachine = new PendulumStateMachineImpl(imgDisplayList, imgStorage, params.getSizeX(), params.getSizeY());

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread serverThread = new Thread(() -> serverThread());
        serverThread.setDaemon(true);
        serverThread.start();

        ImgDisplay imgDisplayFirst = new ImgFirstDisplayImpl();
        imgDisplayFirst.initDisplay(params.getSpiChannel(), params.getSpiAPA102Speed(), params.getSizeX(), params.getLedNum());
        ImgDisplay imgDisplaySecond = new ImgSecondDisplayImpl();
        imgDisplaySecond.initDisplay(params.getSpiChannel(), params.getSpiAPA102Speed(), params.getSizeX(), params.getLedNum());
        imgDisplayList.add(imgDisplayFirst);
        imgDisplayList.add(imgDisplaySecond);

        MPU9250 mpu9250 = new MPU9250(
                new Pi4SPIDeviceSpiSelect(SpiFactory.getInstance(params.getSpiChannel(),
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

    private static void serverThread() {
        try {
            new ServerPendulum(imgStorage);
        } catch (IOException ex) {
            System.err.println("Couldn't start server:\n" + ex);
        }
    }
}