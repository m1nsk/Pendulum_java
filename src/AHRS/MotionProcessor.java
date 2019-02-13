package AHRS;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import devices.Protocol.ProtocolInterface;
import devices.Protocol.i2c.Pi4jI2CDevice;
import devices.sensorImplementations.MPU9250.MPU9250;
import devices.sensors.NineDOF;
import devices.sensors.dataTypes.CircularArrayRing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pendulum.PendulumParams;

import java.io.IOException;

public class MotionProcessor {
    private static final Quaternion rotationQ = new Quaternion(Math.sqrt(2) / 2, - Math.sqrt(2) / 2, 0 ,0);
    private static final Quaternion vertQ = new Quaternion(0.707, 0, -0.707 ,0);
    private static Quaternion closestDelta = new Quaternion(1, 0, 0, 0);
    private static Quaternion closest = new Quaternion(1, 0, 0, 0);
    private static Quaternion computed = new Quaternion(1, 0, 0, 0);

    private static int MOVE_LIMIT_FLAG = 100;

    private CircularArrayRing<DoubleTimeStampedValue> degreeBuffer = new CircularArrayRing<>(MOVE_LIMIT_FLAG);

    private CircularArrayRing<Double> speedBuffer = new CircularArrayRing<>(MOVE_LIMIT_FLAG);

    private PendulumParams params;

    private Ahrs ahrs;

    private NineDOF mpu9250;

    public void init() throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {
        params = PendulumParams.getInstance();
        I2CBus bus = I2CFactory.getInstance(params.getI2cBus());
        ProtocolInterface protocolInterfaceI2C = new Pi4jI2CDevice(bus.getDevice(0x68));
//            ProtocolInterface protocolInterfaceSPI = new Pi4jSPIDevice(SpiFactory.getInstance(
//                    params.getSpiSensorChannel(),
//                    params.getSpiAPA102Speed(),
//                    SpiMode.MODE_0));

        mpu9250 = new MPU9250(
                protocolInterfaceI2C,
                params.getDisplayFrequency());
        ahrs = new Ahrs(mpu9250);
        ahrs.setGyroOffset();
    }

    public void imuLoop() throws IOException, InterruptedException {
        ahrs.imuLoop();
    }

    public Double getSample() {
        Double degree = quaternionToDegree(ahrs.getQ());
//        degreeBuffer.add(new DoubleTimeStampedValue(degree));
//        speedBuffer.add(mpu9250.getGyro().getModule());
//        checkTurn();
        return degree;
    }

    public Double extrapolate() {
        long now  = System.nanoTime();
        if(degreeBuffer.size() > 1) {
            DoubleTimeStampedValue prevData = degreeBuffer.get(0);
            DoubleTimeStampedValue beforePrevData = degreeBuffer.get(1);
            double speed = (prevData.value - beforePrevData.value) / (prevData.nanoTime - beforePrevData.nanoTime);
            return prevData.value + speed * (now - prevData.nanoTime);
        }
        return 0.0;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    private class DoubleTimeStampedValue {

        public DoubleTimeStampedValue(double value) {
            this.value = value;
            this.nanoTime = System.nanoTime();
        }

        public static final long NANOS_PER_SEC = 1000000000;
        private double value;
        private long nanoTime;
    }

    private static boolean slow = false;
    private static int counter = 0;

    private boolean checkTurn() {
         if (mpu9250.getGyro().getModule() < 0.7) {
             if (!slow) {
                 computed = closest;
//                 System.out.println(computed);
                 closestDelta = new Quaternion(1, 0, 0, 0);
             }
             slow = true;
             return false;
         }
         slow = false;
         return true;
    }

    public static Double quaternionToDegree(Quaternion q) {
        Quaternion candidate = Quaternion.multiply(Quaternion.multiply(q, rotationQ), vertQ);
        if(Math.abs(candidate.getWp()) < 0.2) {
            if (Math.abs(candidate.getWp()) < Math.abs(closestDelta.getWp())) {
                closestDelta = candidate;
                closest = q;
                System.out.println(candidate);
            }
        }
        return Quaternion.getYProjectionDegree(Quaternion.multiply(q, rotationQ));
    }
}
