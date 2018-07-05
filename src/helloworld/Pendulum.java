package helloworld;

import AHRS.AHRS;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import devices.Protocol.Pi4SPIDevice;
import devices.sensorImplementations.MPU9250.MPU9250;
import pendulum.ImgDisplay;
import pendulum.ImgStorage;
import pendulum.PendulumParams;
import pendulum.PendulumStateMachine;
import server.ServerPendulum;

import java.io.IOException;


public class Pendulum {

    private static PendulumParams params = new PendulumParams();
    private static ImgStorage imgStorage = new ImgStorage(params);
    private static ImgDisplay imgDisplay = new ImgDisplay();
    private static PendulumStateMachine stateMachine = new PendulumStateMachine(params, imgDisplay, imgStorage);

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread serverThread = new Thread(() -> serverThread());
        serverThread.setDaemon(true);
        serverThread.start();

        imgDisplay.initDisplay(params);

        MPU9250 mpu9250 = new MPU9250(
                new Pi4SPIDevice(SpiFactory.getInstance(params.getSpiChannel(), params.getSpiAPA102Speed(), SpiMode.MODE_0)),
                params.getDisplayFrequency());// sample rate
        AHRS ahrs = new AHRS(mpu9250);
        ahrs.setGyroOffset();

        while (true) {
            ahrs.imuLoop();
            stateMachine.getNewSample(ahrs.getQ());
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
