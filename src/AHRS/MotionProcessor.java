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

    private MovementState state = MovementState.SLOW;

    private static int MOVE_LIMIT_FLAG = 100;

    private CircularArrayRing<DoubleTimeStampedValue> degreeBuffer = new CircularArrayRing<>(MOVE_LIMIT_FLAG);

    private CircularArrayRing<Double> speedBuffer = new CircularArrayRing<>(MOVE_LIMIT_FLAG);

    private PendulumParams params;

    private Ahrs ahrs;

    private NineDOF mpu9250;

    private enum MovementState {
        RIGHT, LEFT, SLOW
    }

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
        Double degree = QuaternionUtils.quaternionToDegree(ahrs.getQ());
        degreeBuffer.add(new DoubleTimeStampedValue(degree));
        speedBuffer.add(mpu9250.getGyro().getModule());
        System.out.println(checkTurn());
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
        return getSample();
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

    private boolean checkTurn() {
        int windowSize = 20;
        int moveTurnLimit = 7;
        if(speedBuffer.size() > windowSize) {
            int size = speedBuffer.size() - 1;
            double start = speedBuffer.get(size - windowSize);
            double middle = speedBuffer.get(size - windowSize / 2);
            double end = speedBuffer.get(size);
            if(Math.abs(start - end) < moveTurnLimit) {
                state = MovementState.SLOW;
            }
            if((start - middle) * (middle - end) > 0)
                return false;
            if(Math.abs(start - middle) < moveTurnLimit || Math.abs(middle - start) < moveTurnLimit)
                return false;
            if(Math.abs(Math.abs(start - middle) - Math.abs(middle - end)) < 100) {
                switch (state) {
                    case LEFT: {
                        state = MovementState.RIGHT;
                        speedBuffer.clear();
                        return true;
                    }
                    case RIGHT: {
                        state = MovementState.LEFT;
                        speedBuffer.clear();
                        return true;
                    }
                    case SLOW: {
                        state = middle - end > 0 ? MovementState.LEFT : MovementState.RIGHT;
                        speedBuffer.clear();
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
