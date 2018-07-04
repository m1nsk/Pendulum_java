package devices.sensorImplementations.MPU9250;

import devices.Protocol.ProtocolInterface;
import devices.sensors.NineDOF;
import devices.sensors.dataTypes.TimestampedData3D;

import java.io.IOException;

/**
 * RPITank Created by MAWood on 17/07/2016.
 */
public class MPU9250 extends NineDOF {

    private static final AccScale accScale = AccScale.AFS_16G;
    private static final GyrScale gyrScale = GyrScale.GFS_2000DPS;

    static final double G_SI = 9.80665;
    static final double PI = 3.14159;

    private final ProtocolInterface mpu9250;

    public MPU9250(ProtocolInterface mpu9250, int sampleRate, int sampleSize) throws IOException, InterruptedException {
        super(sampleRate);
        // get device
        this.mpu9250 = mpu9250;
    }

    public void initialize() throws IOException, InterruptedException {
        selfTest();
//        calibrateGyroAcc();
        initMPU9250();
    }
    
    public float getGyrScale() {
        return gyrScale.getValue();
    }
    
    private void selfTest() throws IOException, InterruptedException {

        byte FS = 0;
        //int bytesRead =0;

        mpu9250.write(Registers.SMPLRT_DIV.getAddress(), (byte) 0x00); // Set gyro sample rate to 1 kHz
        Thread.sleep(2);
        mpu9250.write(Registers.CONFIG.getAddress(), (byte) 0x02); // Set gyro sample rate to 1 kHz and DLPF to 92 Hz
        Thread.sleep(2);
        // Set full scale range for the gyro to 250 dps
        mpu9250.write(Registers.GYRO_CONFIG.getAddress(), (byte) (1 << FS));//GyrScale.GFS_250DPS.getX());
        Thread.sleep(2);
        // Set accelerometer rate to 1 kHz and bandwidth to 92 Hz
        mpu9250.write(Registers.ACCEL_CONFIG2.getAddress(), (byte) 0x02);
        Thread.sleep(2);
        // Set full scale range for the accelerometer to 2 g
        mpu9250.write(Registers.ACCEL_CONFIG.getAddress(), (byte) (1 << FS));// AccScale.AFS_2G.getX());
        Thread.sleep(2);

        final int TEST_LENGTH = 200;

        int[] aAvg = new int[3]; //32 bit integer to accumulate
        int[] gAvg = new int[3];
        short[] registers;
        for (int i = 0; i < 3; i++) {
            aAvg[i] = 0;
            gAvg[i] = 0;
        }
        for (int s = 0; s < TEST_LENGTH; s++) {
            registers = read16BitRegisters(mpu9250, Registers.ACCEL_XOUT_H.getAddress(), 3);
            aAvg[0] += registers[0];
            aAvg[1] += registers[1];
            aAvg[2] += registers[2];
            Thread.sleep(2);

            registers = read16BitRegisters(mpu9250, Registers.GYRO_XOUT_H.getAddress(), 3);
            gAvg[0] += registers[0];
            gAvg[1] += registers[1];
            gAvg[2] += registers[2];
            Thread.sleep(2);
        }

        for (int i = 0; i < 3; i++) {
            aAvg[i] /= TEST_LENGTH;
            gAvg[i] /= TEST_LENGTH;
        }
        // Configure the accelerometer for self-test
        mpu9250.write(Registers.ACCEL_CONFIG.getAddress(), (byte) 0xE0); // Enable self test on all three axes and set accelerometer range to +/- 2 g
        Thread.sleep(2);
        mpu9250.write(Registers.GYRO_CONFIG.getAddress(), (byte) 0xE0);// Enable self test on all three axes and set gyro range to +/- 250 degrees/s
        Thread.sleep(25); // Delay a while to let the device stabilise

        int[] aSTAvg = new int[3]; // cumulative values hence int to avoid overflow
        int[] gSTAvg = new int[3];

        // get average self-test values of gyro and accelerometer
        for (int s = 0; s < TEST_LENGTH; s++) {
            registers = read16BitRegisters(mpu9250, Registers.ACCEL_XOUT_H.getAddress(), 3);
            aSTAvg[0] += registers[0];
            aSTAvg[1] += registers[1];
            aSTAvg[2] += registers[2];
            Thread.sleep(2);

            registers = read16BitRegisters(mpu9250, Registers.GYRO_XOUT_H.getAddress(), 3);
            gSTAvg[0] += registers[0];
            gSTAvg[1] += registers[1];
            gSTAvg[2] += registers[2];
            Thread.sleep(2);
        }

        for (int i = 0; i < 3; i++) {
            aSTAvg[i] /= TEST_LENGTH;
            gSTAvg[i] /= TEST_LENGTH;
        }

        Thread.sleep(2);
        mpu9250.write(Registers.GYRO_CONFIG.getAddress(), GyrScale.GFS_250DPS.getValue());
        Thread.sleep(2);
        mpu9250.write(Registers.ACCEL_CONFIG.getAddress(), AccScale.AFS_2G.getValue());
        Thread.sleep(25); // Delay a while to let the device stabilise

        byte[] selfTest = new byte[6];

        selfTest[0] = mpu9250.read(Registers.SELF_TEST_X_ACCEL.getAddress());
        Thread.sleep(2);
        selfTest[1] = mpu9250.read(Registers.SELF_TEST_Y_ACCEL.getAddress());
        Thread.sleep(2);
        selfTest[2] = mpu9250.read(Registers.SELF_TEST_Z_ACCEL.getAddress());
        Thread.sleep(2);

        selfTest[3] = mpu9250.read(Registers.SELF_TEST_X_GYRO.getAddress());
        Thread.sleep(2);
        selfTest[4] = mpu9250.read(Registers.SELF_TEST_Y_GYRO.getAddress());
        Thread.sleep(2);
        selfTest[5] = mpu9250.read(Registers.SELF_TEST_Z_GYRO.getAddress());
        Thread.sleep(2);

        float[] factoryTrim = new float[6];

        factoryTrim[0] = (float) (2620 / 1 << FS) * (float) Math.pow(1.01, (float) selfTest[0] - 1f);
        factoryTrim[1] = (float) (2620 / 1 << FS) * (float) Math.pow(1.01, (float) selfTest[1] - 1f);
        factoryTrim[2] = (float) (2620 / 1 << FS) * (float) Math.pow(1.01, (float) selfTest[2] - 1f);
        factoryTrim[3] = (float) (2620 / 1 << FS) * (float) Math.pow(1.01, (float) selfTest[3] - 1f);
        factoryTrim[4] = (float) (2620 / 1 << FS) * (float) Math.pow(1.01, (float) selfTest[4] - 1f);
        factoryTrim[5] = (float) (2620 / 1 << FS) * (float) Math.pow(1.01, (float) selfTest[5] - 1f);

        float aXAccuracy = 100 * ((float) (aSTAvg[0] - aAvg[0])) / factoryTrim[0];
        float aYAccuracy = 100 * ((float) (aSTAvg[1] - aAvg[1])) / factoryTrim[1];
        float aZAccuracy = 100 * ((float) (aSTAvg[2] - aAvg[2])) / factoryTrim[2];

        System.out.println("Accelerometer accuracy:(% away from factory values)");
        System.out.println("x: " + aXAccuracy + "%");
        System.out.println("y: " + aYAccuracy + "%");
        System.out.println("z: " + aZAccuracy + "%");
        System.out.println("Gyroscope accuracy:(% away from factory values)");
        System.out.println("x: " + 100.0 * ((float) (gSTAvg[0] - gAvg[0])) / factoryTrim[3] + "%");
        System.out.println("y: " + 100.0 * ((float) (gSTAvg[1] - gAvg[1])) / factoryTrim[4] + "%");
        System.out.println("z: " + 100.0 * ((float) (gSTAvg[2] - gAvg[2])) / factoryTrim[5] + "%");
    }

    private void initMPU9250() throws IOException, InterruptedException {
        int MPU_Init_Data[][] = {
//            {0x80, Registers.PWR_MGMT_1.getAddress()},     // Reset Device - Disabled because it seems to corrupt initialisation of AK8963
            {0x01, Registers.PWR_MGMT_1.getAddress()}, // Clock Source
            {0x00, Registers.PWR_MGMT_2.getAddress()}, // Enable Acc & Gyro
            {0x00, Registers.CONFIG.getAddress()}, // Use DLPF set Gyroscope bandwidth 184Hz, temperature bandwidth 188Hz
            {0x18, Registers.GYRO_CONFIG.getAddress()}, // +-2000dps
            {3 << 3, Registers.ACCEL_CONFIG.getAddress()}, // +-16G
            {0x08, Registers.ACCEL_CONFIG2.getAddress()}, // Set Acc Data Rates, Enable Acc LPF , Bandwidth 184Hz
            {0x30, Registers.INT_PIN_CFG.getAddress()}, //
            {0x20, Registers.USER_CTRL.getAddress()}, // I2C Master mode
            {0x0D, Registers.I2C_MST_CTRL.getAddress()}, //  I2C configuration multi-master  IIC 400KHz
            {0x12, Registers.I2C_SLV0_DO.getAddress()}, // Register value to continuous measurement in 16bit
            {0x81, Registers.I2C_SLV0_CTRL.getAddress()} //Enable I2C and set 1 byte
        };

        byte c = 0;
        // Set accelerometer full-scale range configuration
        c = (byte) mpu9250.read(Registers.ACCEL_CONFIG.getAddress()); // get current ACCEL_CONFIG register value
        // c = c & ~0xE0; // Clear self-test bits [7:5]
        c = (byte) (c & ~0x18);  // Clear AFS bits [4:3]
        c = (byte) (c | accScale.getValue() << 3); // Set full scale range for the accelerometer
        mpu9250.write(Registers.ACCEL_CONFIG.getAddress(), c); // Write new ACCEL_CONFIG register value

        // Set accelerometer sample rate configuration
        // It is possible to get a 4 kHz sample rate from the accelerometer by choosing 1 for
        // accel_fchoice_b bit [3]; in this case the bandwidth is 1.13 kHz
        c = (byte) mpu9250.read(Registers.ACCEL_CONFIG2.getAddress()); // get current ACCEL_CONFIG2 register value
        c = (byte) (c & ~0x0F); // Clear accel_fchoice_b (bit 3) and A_DLPFG (bits [2:0])
        c = (byte) (c | 0x03);  // Set accelerometer rate to 1 kHz and bandwidth to 41 Hz
        mpu9250.write(Registers.ACCEL_CONFIG2.getAddress(), c); // Write new ACCEL_CONFIG2 register value

        for (int i = 0; i < MPU_Init_Data.length; i++) {
            mpu9250.write(MPU_Init_Data[i][1], (byte) MPU_Init_Data[i][0]);
        }
        Thread.sleep(100);
    }
    
    @Override
    public void updateSensorData() throws IOException {
        float x, y, z;
        short registers[] = new short[3];
        registers = read16BitRegisters(mpu9250, Registers.ACCEL_XOUT_H.getAddress(), 3);
//        System.out.println(registers[0] + " " + registers[1] + " " + registers[2]);

        x = (float) ((registers[0]) * accScale.getRes() * G_SI); // transform from raw data to g
        y = (float) ((registers[1]) * accScale.getRes() * G_SI); // transform from raw data to g
        z = (float) ((registers[2]) * accScale.getRes() * G_SI); // transform from raw data to g

        x -= accBias[0];
        y -= accBias[1];
        z -= accBias[2];

        acc = new TimestampedData3D(x, y, z);

//        System.out.println(acc);
        registers = read16BitRegisters(mpu9250, Registers.GYRO_XOUT_H.getAddress(), 3);
//        System.out.println(registers[0] + " " + registers[1] + " " + registers[2] );

        x = (float) ((float) registers[0] * gyrScale.getRes() * (PI / 180)); // transform from raw data to degrees/s
        y = (float) ((float) registers[1] * gyrScale.getRes() * (PI / 180)); // transform from raw data to degrees/s
        z = (float) ((float) registers[2] * gyrScale.getRes() * (PI / 180)); // transform from raw data to degrees/s

        gyr = new TimestampedData3D(x, y, z);
//        System.out.println(gyr);

        byte rawData[] = mpu9250.read(Registers.TEMP_OUT_H.getAddress(), 2);  // Read the two raw data registers sequentially into data array

        mpu9250.read(Registers.TEMP_OUT_H.getAddress(), 2);  // Read again to trigger
        therm = ((float) (short) ((rawData[0] << 8) | rawData[1]));  // Turn the MSB and LSB into a 16-bit value
    }

    /**
     * Reads the specified number of 16 bit Registers from a given device and
     * address
     *
     * @param address - the start address for the read
     * @param regCount - number of 16 bit registers to be read
     * @return - an array of shorts (16 bit signed values) holding the registers
     * Each registers is constructed from reading and combining 2 bytes, the
     * first byte forms the more significant part of the register
     */
    public short[] read16BitRegisters(ProtocolInterface device, int address, int regCount) {
        byte rawData[] = null;
        try {
            rawData = device.read(address, regCount * 2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        short[] registers = new short[regCount];
        for (int i = 0; i < regCount; i++) {
            registers[i] = (short) ((short) ((rawData[i * 2] + 256) % 256 << 8) | (short) ((rawData[i * 2 + 1] + 256) % 256));  // Turn the MSB and LSB into a signed 16-bit value
        }
        return registers;
    }
}
