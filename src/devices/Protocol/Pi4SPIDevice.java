package devices.Protocol;

import com.pi4j.wiringpi.Spi;

import java.io.IOException;

/**
 * Created by MAWood on 17/07/2016.
 */
public class Pi4SPIDevice implements ProtocolInterface
{
    private final int spiChannel;

    public static int READ_CMD = 0x80;

    public Pi4SPIDevice() {
        this.spiChannel = 0;
        Spi.wiringPiSPISetup(spiChannel, 1_000_000);
    }

    public Pi4SPIDevice(int spiChannel, int spiSpeed)
    {
        this.spiChannel = spiSpeed;
        Spi.wiringPiSPISetup(spiChannel, spiSpeed);
    }

    @Override
    public byte read(int address) throws IOException
    {
        byte packet[] = new byte[2];
        packet[0] = (byte) (address | READ_CMD);
        packet[1] = 0b00000000;

        Spi.wiringPiSPIDataRW(0, packet, 2);
        return packet[0];
    }

    @Override
    public byte[] read(int address, int count) throws IOException
    {
        byte packet[] = new byte[count];
//        packet[0] = (byte) (address | READ_CMD);
//        Spi.wiringPiSPIDataRW(spiChannel, packet);
        for (int i = 0; i < count; i += 2) {
            byte subPacket[] = new byte[2];
            subPacket[0] = (byte) (address + i | READ_CMD);
            subPacket[1] = 0b00000000;
            Spi.wiringPiSPIDataRW(0, subPacket, 2);
            System.arraycopy(subPacket, 0, packet, i, 2);
        }
        return packet;
    }

    @Override
    public void write(int address, byte data) throws IOException
    {
        byte packet[] = new byte[2];
        packet[0] = (byte)address;  // register byte
        packet[1] = data; // data byte

        int result = Spi.wiringPiSPIDataRW(spiChannel, packet, 2);
        
    }
}
