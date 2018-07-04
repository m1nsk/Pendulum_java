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
import pendulum.ImgStorage;
import server.ServerPendulum;


public class main {
    final static ImgStorage imgStorage = new ImgStorage(360, 100);

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

        Quaternion vertQ = new Quaternion((float) Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0);
        MPU9250 mpu9250 = new MPU9250(
                new Pi4SPIDevice(SpiFactory.getInstance(SpiChannel.CS0, 10_000_000, SpiMode.MODE_0)), // MPU9250 Protocol device
                200, // sample rate
                100);                                   // sample size
        AHRS ahrs = new AHRS(mpu9250);
        ahrs.setGyroOffset();

        Apa102Output.initSpi(SpiChannel.CS0, 10_000_000, SpiMode.MODE_0);

        Apa102Output strip = new Apa102Output(NUM_LEDS);

        byte[] ledRGBs = new byte[NUM_LEDS * 3];
        int counter = 0;
        while (true) {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 1000) {
                counter++;
                ahrs.imuLoop();
                float angel = ahrs.getQ().multiply(vertQ).getD();
                Thread.sleep(5);
            }
            counter = 0;
        }
    }
}
