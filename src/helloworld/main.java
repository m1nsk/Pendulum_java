package helloworld;

import AHRS.AHRS;
import AHRS.Quaternion;
import java.io.IOException;

import devices.Protocol.Pi4SPIDevice;
import devices.sensorImplementations.MPU9250.MPU9250;

import com.github.dlopuch.apa102_java_rpi.Apa102Output;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import pendulum.*;
import server.ServerPendulum;


public class main {

    private static PendulumParams params = new PendulumParams();
    private static ImgStorage imgStorage = new ImgStorage(params);

    final static int NUM_LEDS = 144;

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread serverThread = new Thread(() -> {
            try {
                new ServerPendulum(imgStorage);
            } catch (IOException ex) {
                System.err.println("Couldn't start server:\n" + ex);
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        ImgDisplay imgDisplay = new ImgDisplay();
        imgDisplay.initDisplay(params);

        PendulumStateMachine stateMachine = new PendulumStateMachine(params, imgDisplay, imgStorage);

        MPU9250 mpu9250 = new MPU9250(
                new Pi4SPIDevice(SpiFactory.getInstance(SpiChannel.CS0, 10_000_000, SpiMode.MODE_0)), // MPU9250 Protocol device
                200, // sample rate
                100);// sample size
        AHRS ahrs = new AHRS(mpu9250);
        ahrs.setGyroOffset();


        byte[] ledRGBs = new byte[NUM_LEDS * 3];
        while (true) {
            ahrs.imuLoop();
            stateMachine.getNewSample(ahrs.getQ());
            Thread.sleep(1000 / params.getDisplayFrequency());
        }
    }
}
