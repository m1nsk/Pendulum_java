package devices.sensorImplementations.MPU9250;

public enum AccScale
{
    AFS_2G(0x00,2),
    AFS_4G(0x08,4),
    AFS_8G(0x10,8),
    AFS_16G(0x18,16);

    private final int value;
    private final int minMax;
    AccScale(int value, int minMax)
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
