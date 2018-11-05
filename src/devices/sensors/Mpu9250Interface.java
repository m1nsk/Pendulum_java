/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.sensors;

import devices.sensors.dataTypes.TimestampedData3D;
import java.io.IOException;

/**
 *
 * @author minsk
 */
public interface Mpu9250Interface {

    void initialize() throws IOException, InterruptedException ;

    TimestampedData3D getGyro();

    TimestampedData3D getAccel();

    void updateSensorData() throws IOException;
}
