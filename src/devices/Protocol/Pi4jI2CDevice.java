package devices.Protocol;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

/**
 *
 * @author minsk
 */
public class Pi4jI2CDevice implements ProtocolInterface
{
    private final I2CDevice device;

    public Pi4jI2CDevice(I2CDevice device)
    {
        this.device = device;
    }

    @Override
    public byte read(int address) throws IOException
    {
        return (byte) device.read(address);
    }

    @Override
    public byte[] read(int address, int count) throws IOException
    {
        byte[] buffer = new byte[count];
        device.read(address,buffer,0,count);
        return buffer;
    }

    @Override
    public void write(int address, byte data) throws IOException
    {
        device.write(address,data);
    }
}
