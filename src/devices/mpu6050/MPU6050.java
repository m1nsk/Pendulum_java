package devices.mpu6050;

import devices.sensors.AccScale;
import devices.sensors.GyrScale;
import devices.Protocol.ProtocolInterface;
import devices.sensors.NineDOF;
import devices.sensors.dataTypes.TimestampedData3D;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 *
 * @author minsk
 */
@Slf4j
public class MPU6050 extends NineDOF {

    private final AccScale accScale = AccScale.AFS_16G;
    private final GyrScale gyrScale = GyrScale.GFS_2000DPS;

    final double G_SI = 9.80665;
    final double PI = 3.14159;

    private int counter = 0;

    private final ProtocolInterface mpu6050;

    public MPU6050(ProtocolInterface mpu6050, int sampleRate) {
        super(sampleRate);
        this.mpu6050 = mpu6050;
    }

    @Override
    public void initialize() {
        initMPU6050();
    }

    @SneakyThrows
    private void initMPU6050() {

        //1 Waking the device up
        writeConfigRegisterAndValidate(
                "Waking up device",
                "Wake-up config successfully written: ",
                Mpu6050Registers.MPU6050_RA_PWR_MGMT_1,
                Mpu6050RegisterValues.MPU6050_RA_PWR_MGMT_1);

        //2 Configure sample rate
        writeConfigRegisterAndValidate(
                "Configuring sample rate",
                "Sample rate successfully written: ",
                Mpu6050Registers.MPU6050_RA_SMPLRT_DIV,
                Mpu6050RegisterValues.MPU6050_RA_SMPLRT_DIV);

        //3 Setting global config
        writeConfigRegisterAndValidate(
                "Setting global config (digital low pass filter)",
                "Global config successfully written: ",
                Mpu6050Registers.MPU6050_RA_CONFIG,
                Mpu6050RegisterValues.MPU6050_RA_CONFIG);

        //4 Configure Gyroscope
        writeConfigRegisterAndValidate(
                "Configuring gyroscope",
                "Gyroscope config successfully written: ",
                Mpu6050Registers.MPU6050_RA_GYRO_CONFIG,
                Mpu6050RegisterValues.MPU6050_RA_GYRO_CONFIG);

        //5 Configure Accelerometer
        writeConfigRegisterAndValidate(
                "Configuring accelerometer",
                "Accelerometer config successfully written: ",
                Mpu6050Registers.MPU6050_RA_ACCEL_CONFIG,
                Mpu6050RegisterValues.MPU6050_RA_ACCEL_CONFIG);

        //6 Configure interrupts
        writeConfigRegisterAndValidate(
                "Configuring interrupts",
                "Interrupt config successfully written: ",
                Mpu6050Registers.MPU6050_RA_INT_ENABLE,
                Mpu6050RegisterValues.MPU6050_RA_INT_ENABLE);

        //7 Configure low power operations
        writeConfigRegisterAndValidate(
                "Configuring low power operations",
                "Low power operation config successfully written: ",
                Mpu6050Registers.MPU6050_RA_PWR_MGMT_2,
                Mpu6050RegisterValues.MPU6050_RA_PWR_MGMT_2);

        for (byte i = 1; i <= 120; i++) {
            byte registerData = mpu6050.read(i);
            log.debug(i + "\t\tRegisterData:" + formatBinary(registerData));
        }

        Thread.sleep(100);
    }

    @Override
    public void updateSensorData() {
        float x, y, z;
        short registers[] = new short[6];
        registers = read16BitRegisters(mpu6050, Mpu6050Registers.MPU6050_RA_ACCEL_XOUT_H, 7);

        x = (float) ((registers[0]) * accScale.getRes() * G_SI); // transform from raw data to g
        y = (float) ((registers[1]) * accScale.getRes() * G_SI); // transform from raw data to g
        z = (float) ((registers[2]) * accScale.getRes() * G_SI); // transform from raw data to g

        x -= accBias[0];
        y -= accBias[1];
        z -= accBias[2];

        acc = new TimestampedData3D(x, y, z);

        x = (float) ((float) registers[4] * gyrScale.getRes() * (PI / 180)); // transform from raw data to degrees/s
        y = (float) ((float) registers[5] * gyrScale.getRes() * (PI / 180)); // transform from raw data to degrees/s
        z = (float) ((float) registers[6] * gyrScale.getRes() * (PI / 180)); // transform from raw data to degrees/s

        gyr = new TimestampedData3D(x, y, z);
 }


    public short[] read16BitRegisters(ProtocolInterface device, int address, int regCount) {
        byte rawData[] = null;
        while (rawData == null) {
            try {
                rawData = device.read(address, regCount * 2);
            } catch (IOException ignored) {
                log.debug("Read register data try counter: {}", ++counter);
            }
        }
        short[] registers = new short[regCount];
        for (int i = 0; i < regCount; i++) {
            registers[i] = (short) ((short) ((rawData[i * 2] + 256) % 256 << 8) | (short) ((rawData[i * 2 + 1] + 256) % 256));  // Turn the MSB and LSB into a signed 16-bit value
        }
        return registers;
    }

    @SneakyThrows
    public void writeConfigRegisterAndValidate(String initialText, String successText, byte register, byte registerData) {
        log.debug(initialText);
        mpu6050.write(register, registerData);
        byte returnedRegisterData = mpu6050.read(register);
        if (returnedRegisterData == registerData) {
            log.debug(successText + formatBinary(returnedRegisterData));
        } else {
            throw new RuntimeException("Tried to write " + formatBinary(registerData) + " to "
                    + register + ", but validiating value returned " + formatBinary(returnedRegisterData));
        }
    }

    public static String formatBinary(byte b) {
        String binaryString = Integer.toBinaryString(b);
        if (binaryString.length() > 8) {
            binaryString = binaryString.substring(binaryString.length() - 8);
        }
        if (binaryString.length() < 8) {
            byte fillingZeros = (byte) (8 - binaryString.length());
            for (int j = 1; j <= fillingZeros; j++) {
                binaryString = "0" + binaryString;
            }
        }
        return binaryString;
    }
}
