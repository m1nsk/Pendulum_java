package devices.sensors;

import devices.sensors.dataTypes.TimestampedData3D;

/**
 * Created by MAWood on 17/07/2016.
 */
public abstract class NineDOF extends SensorPackage implements Mpu9250Interface
{
    protected TimestampedData3D acc;
    protected TimestampedData3D gyr;
    protected float therm;
    
    protected float[] accBias;
    protected float[] gyrBias;
    protected float[] magBias;
    protected float thermBias;

    protected float[] accScaling;
    protected float[] gyrScaling;
    protected float[] magScaling;
    protected float thermScaling;

    protected NineDOF(int sampleRate)
    {
        super(sampleRate);
        accBias = new float[]{0,0,0};
        gyrBias = new float[]{0,0,0};
        magBias = new float[]{0,0,0};
        thermBias = 0f;

        accScaling = new float[]{1,1,1};
        gyrScaling = new float[]{1,1,1};
        magScaling = new float[]{1,1,1};
        
        acc = new TimestampedData3D(0,0,0);
        gyr = new TimestampedData3D(0,0,0);
        therm = 0;
        
        thermScaling = 1f;
    }

    @Override
    public float getTemperature()
    {
        return therm;
    }

    @Override
    public TimestampedData3D getGyro()
    {
        return gyr;
    }
    
    @Override
    public TimestampedData3D getAccel()
    {
        return acc;
    }

    public void updateData()
    {
        try
        {
            updateSensorData();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
