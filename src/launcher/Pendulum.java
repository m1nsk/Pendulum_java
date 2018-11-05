package launcher;

import AHRS.AHRS;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import devices.Protocol.spi.Pi4SPIDevice;
import devices.sensorImplementations.MPU9250.MPU9250;
import pendulum.PendulumParams;
import pendulum.display.ImgDisplay;
import pendulum.display.impl.ImgDefaultDisplayImpl;
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

        ImgDisplay imgDefaultDisplay = new ImgDefaultDisplayImpl(params.getSpiApa102Channel(), params.getSpiAPA102Speed(), params.getSizeX(), params.getLedNum());
        imgDisplayList.add(imgDefaultDisplay);


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
//            System.out.println(ahrs.getQ());
            Thread.sleep(2000 / params.getDisplayFrequency());
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
