package devices.Protocol.spi;

import com.pi4j.io.spi.SpiDevice;
import devices.Protocol.ProtocolInterface;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by MAWood on 17/07/2016.
 */
public class Pi4SPIDevice implements ProtocolInterface
{
   private final SpiDevice device;
   
   
    public static int READ_CMD = 0x80;

    public Pi4SPIDevice(SpiDevice device)
    {
        this.device = device;
    }

    @Override
    public byte read(int address) throws IOException
    {
        byte[] data = new byte[2];
        data[0] = (byte)(address | READ_CMD);
        byte[] result = device.write(data);
        return result[1];
    }

    @Override
    public byte[] read(int address, int count) throws IOException
    {
        byte[] result = new byte[count];
        byte[] buffer = new byte[count + 1];
        buffer[0] = (byte)(address | READ_CMD);
        System.arraycopy(device.write(buffer), 1, result, 0, count);       
        return result;
    }

    @Override
    public void write(int address, byte data) throws IOException
    {
        device.write((byte)address,data);
//        System.out.println("address " + address + " data: " + data + " checked: " + read(address));
    }
}
