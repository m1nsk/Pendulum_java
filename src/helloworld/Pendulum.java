package helloworld;

import AHRS.AHRS;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import devices.Protocol.Pi4SPIDevice;
import devices.sensorImplementations.MPU9250.MPU9250;
import pendulum.ImgDisplay;
import pendulum.ImgStorage;
import pendulum.Impl.ImgDisplayTwoLinesImpl;
import pendulum.Impl.ImgStorageImpl;
import pendulum.Impl.PendulumParams;
import pendulum.Impl.PendulumStateMachineImpl;
import pendulum.PendulumStateMachine;
import server.ServerPendulum;

import java.io.IOException;


public class Pendulum {

    private static PendulumParams params = new PendulumParams();
    private static ImgStorage imgStorage = new ImgStorageImpl(params.getSizeX(), params.getSizeY());
    private static ImgDisplay imgDisplay = new ImgDisplayTwoLinesImpl();
    private static PendulumStateMachine stateMachine = new PendulumStateMachineImpl(imgDisplay, imgStorage, params.getSizeX(), params.getSizeY());

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread serverThread = new Thread(() -> serverThread());
        serverThread.setDaemon(true);
        serverThread.start();

        imgDisplay.initDisplay(params.getSpiChannel(), params.getSpiAPA102Speed(), params.getSizeX(), params.getLedNum());

        MPU9250 mpu9250 = new MPU9250(
                new Pi4SPIDevice(SpiFactory.getInstance(params.getSpiChannel(), params.getSpiAPA102Speed(), SpiMode.MODE_0)),
                params.getDisplayFrequency());// sample rate
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
