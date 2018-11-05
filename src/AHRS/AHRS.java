/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AHRS;

import devices.sensorImplementations.MPU9250.MPU9250;
import devices.sensors.Mpu9250Interface;
import devices.sensors.dataTypes.TimestampedData3D;
import java.io.IOException;

/**
 *
 * @author minsk
 */
public class AHRS {

    Mpu9250Interface sensor = null;
    private float maxdt;
    private float mindt = 0.01f;
    private boolean isFirst = false;
    private long previoustime, currenttime;
    private final static float G_SI = 9.80665f;
    private final static float PI = 3.14159f;
    private float gyroOffset[] = new float[3];
    private float q0, q1, q2, q3;
    private final float twoKp;
    private final float twoKi;
    private float integralFBx;
    private float integralFBy;
    private float integralFBz;

    public AHRS(Mpu9250Interface sensor) {
        this.sensor = sensor;
        q0 = 1;
        q1 = 0;
        q2 = 0;
        q3 = 0;
        twoKi = 0;
        twoKp = 2;
    }

    public void imuLoop() throws InterruptedException, IOException {
        
        float dt;        // Timing data

        //----------------------- Calculate delta time ----------------------------
        previoustime = currenttime;
        currenttime = System.nanoTime() / 1000;
        dt = (float) ((currenttime - previoustime) / 1000000.0);
        if (dt < 1.0 / 1300.0) {
            Thread.sleep((long) ((1 / 1300 - dt) * 1000000));
        }
        currenttime = System.nanoTime() / 1000;
        dt = (float) ((currenttime - previoustime) / 1000000.0);
//        System.out.println(dt);

        //-------- Read raw measurements from the MPU and update AHRS --------------
        updateIMU(dt);

        if (!isFirst) {
            if (dt > maxdt) {
                maxdt = dt;
            }
            if (dt < mindt) {
                mindt = dt;
            }
        }
        isFirst = false;

    }

    public void updateIMU(float dt) throws IOException {
        float recipNorm;
        float halfvx, halfvy, halfvz;
        float halfex, halfey, halfez;
        float qa, qb, qc;

        float ax, ay, az;
        float gx, gy, gz;

        // Accel + gyro.
        sensor.updateSensorData();
        TimestampedData3D acc = sensor.getAccel();
        ax = acc.getX();
        ay = acc.getY();
        az = acc.getZ();

        TimestampedData3D gyr = sensor.getGyro();
        gx = gyr.getX();
        gy = gyr.getY();
        gz = gyr.getZ();

        ax /= G_SI;
        ay /= G_SI;
        az /= G_SI;
        gx *= (180 / PI) * 0.0175;
        gy *= (180 / PI) * 0.0175;
        gz *= (180 / PI) * 0.0175;

        gx -= gyroOffset[0];
        gy -= gyroOffset[1];
        gz -= gyroOffset[2];

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Estimated direction of gravity and vector perpendicular to magnetic flux
            halfvx = q1 * q3 - q0 * q2;
            halfvy = q0 * q1 + q2 * q3;
            halfvz = q0 * q0 - 0.5f + q3 * q3;

            // Error is sum of cross product between estimated and measured direction of gravity
            halfex = (ay * halfvz - az * halfvy);
            halfey = (az * halfvx - ax * halfvz);
            halfez = (ax * halfvy - ay * halfvx);

            // Compute and apply integral feedback if enabled
            if(twoKi > 0.0f) {
                integralFBx += twoKi * halfex * dt;	// integral error scaled by Ki
                integralFBy += twoKi * halfey * dt;
                integralFBz += twoKi * halfez * dt;
                gx += integralFBx;	// apply integral feedback
                gy += integralFBy;
                gz += integralFBz;
            }
            else {
                integralFBx = 0.0f;	// prevent integral windup
                integralFBy = 0.0f;
                integralFBz = 0.0f;
            }

            // Apply proportional feedback
            gx += twoKp * halfex;
            gy += twoKp * halfey;
            gz += twoKp * halfez;
        }

        // Integrate rate of change of quaternion
        gx *= (0.5f * dt);		// pre-multiply common factors
        gy *= (0.5f * dt);
        gz *= (0.5f * dt);
        qa = q0;
        qb = q1;
        qc = q2;
        q0 += (-qb * gx - qc * gy - q3 * gz);
        q1 += (qa * gx + qc * gz - q3 * gy);
        q2 += (qa * gy - qb * gz + q3 * gx);
        q3 += (qa * gz + qb * gy - qc * gx);

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;
    }

    private static float invSqrt(float x) {
        return (float) (1.0f / Math.sqrt(x));
    }

    public void setGyroOffset() throws IOException, InterruptedException {
        //---------------------- Calculate the offset -----------------------------

        float offset[] = {0.0f, 0.0f, 0.0f};
        float gx, gy, gz;

        //----------------------- MPU initialization ------------------------------
        sensor.initialize();

        //-------------------------------------------------------------------------
        System.out.println("Beginning Gyro calibration...\n");
        for (int i = 0; i < 100; i++) {
            sensor.updateSensorData();
            TimestampedData3D gyr = sensor.getGyro();
            gx = gyr.getX();
            gy = gyr.getY();
            gz = gyr.getZ();

            gx *= 180 / PI;
            gy *= 180 / PI;
            gz *= 180 / PI;

            offset[0] += gx * 0.0175;
            offset[1] += gy * 0.0175;
            offset[2] += gz * 0.0175;

            Thread.sleep(10);
        }
        offset[0] /= 100.0;
        offset[1] /= 100.0;
        offset[2] /= 100.0;
        
        System.out.println("Offsets are: " + offset[0] + " " + offset[1] + " " + offset[2]);

        gyroOffset[0] = offset[0];
        gyroOffset[1] = offset[1];
        gyroOffset[2] = offset[2];
    }
    
    public Quaternion getQ() {
        return new Quaternion(q0, q1, q2, q3);
    }
}
