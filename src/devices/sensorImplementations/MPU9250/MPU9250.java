package devices.sensorImplementations.MPU9250;

import devices.Protocol.ProtocolInterface;
import devices.sensors.NineDOF;
import devices.sensors.dataTypes.TimestampedData3D;

import java.io.IOException;

/**
 *
 * @author minsk
 */
public class MPU9250 extends NineDOF {

    private static final AccScale accScale = AccScale.AFS_16G;
    private static final GyrScale gyrScale = GyrScale.GFS_1000DPS;

    static final double G_SI = 9.80665;
    static final double PI = 3.14159;

    private final ProtocolInterface mpu9250;

    public MPU9250(ProtocolInterface mpu9250, int sampleRate) {
        super(sampleRate);
        this.mpu9250 = mpu9250;
    }

    public void initialize() throws IOException, InterruptedException {
        initMPU9250();
    }

    private void initMPU9250() throws IOException, InterruptedException {
        int MPU_Init_Data[][] = {
                //{0x80, Registers.PWR_MGMT_1},     // Reset Device - Disabled because it seems to corrupt initialisation of AK8963
                {0x01, Registers.PWR_MGMT_1.getAddress()},     // Clock Source
                {0x00, Registers.PWR_MGMT_2.getAddress()},     // Enable Acc & Gyro
                {0x00, Registers.CONFIG.getAddress()},         // Use DLPF set Gyroscope bandwidth 184Hz, temperature bandwidth 188Hz
                {0x18, Registers.GYRO_CONFIG.getAddress()},    // +-2000dps
                {3<<3, Registers.ACCEL_CONFIG.getAddress()},   // +-16G
                {0x08, Registers.ACCEL_CONFIG2.getAddress()}, // Set Acc Data Rates, Enable Acc LPF , Bandwidth 184Hz
                {0x30, Registers.INT_PIN_CFG.getAddress()}
        };

        for (int i = 0; i < MPU_Init_Data.length; i++) {
            mpu9250.write(MPU_Init_Data[i][1], (byte) MPU_Init_Data[i][0]);
        }


        setAccScale(accScale);
        setGyroScale(gyrScale);

        Thread.sleep(100);
    }

    private void setGyroScale(GyrScale gyrScale) throws IOException {
        mpu9250.write(Registers.GYRO_CONFIG.getAddress(), gyrScale.getValue());
    }

    private void setAccScale(AccScale accScale) throws IOException {
        mpu9250.write(Registers.ACCEL_CONFIG.getAddress(), accScale.getValue());
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


    public short[] read16BitRegisters(ProtocolInterface device, int address, int regCount) {
        byte rawData[] = null;
        try {
            rawData = device.read(address, regCount * 2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        if(address == 59) {
//            System.out.println(rawData[0]);
//        }
        short[] registers = new short[regCount];
        for (int i = 0; i < regCount; i++) {
            registers[i] = (short) ((short) ((rawData[i * 2] + 256) % 256 << 8) | (short) ((rawData[i * 2 + 1] + 256) % 256));  // Turn the MSB and LSB into a signed 16-bit value
        }
        return registers;
    }
}
