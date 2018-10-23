package devices.sensorImplementations.MPU9250;

public enum GyrScale
{
    GFS_250DPS(0x00,250),
    GFS_500DPS(0x08,500),
    GFS_1000DPS(0x10,1000),
    GFS_2000DPS(0x18,2000);


    private final int value;
    private final int minMax;
    GyrScale(int value, int minMax)
    {
        this.value = value;
        this.minMax = minMax;
    }
    public byte getValue()
    {
        return (byte)value;
    }
    public double getRes()
    {
        return (double)minMax/32768.0;
    }
    public int getMinMax()
    {
        return minMax;
    }
}
