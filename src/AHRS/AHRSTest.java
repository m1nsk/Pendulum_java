package AHRS;

import devices.sensors.Mpu9250Interface;

import java.io.IOException;

public class AHRSTest {

    private static AHRS ahrs;

    public static void main(String[] args) throws IOException, InterruptedException {
        Mpu9250Interface sensor = new MOCSensor();
        sensor.initialize();
        ahrs = new AHRS(new MOCSensor());
        ahrs.setGyroOffset();
        ahrs.updateIMU(3348.5F);
        System.out.println(ahrs.getQ());
        ahrs.updateIMU(0.01285F);
        System.out.println(ahrs.getQ());
    }
}
