package devices.Protocol.spi;

import com.pi4j.io.spi.SpiDevice;
import pendulum.SpiSwitcher;
import java.io.IOException;
/**
 *
 * @author minsk
 */

public class Pi4SPIDeviceSpiSelect extends Pi4SPIDevice {
    public Pi4SPIDeviceSpiSelect(SpiDevice device) {
        super(device);
    }

    @Override
    public byte read(int address) throws IOException {
        SpiSwitcher.setAPAFirst();
        byte result = super.read(address);
        SpiSwitcher.setNone();
        return result;
    }

    @Override
    public byte[] read(int address, int count) throws IOException {
        SpiSwitcher.setAPAFirst();
        byte[] result = super.read(address, count);
        SpiSwitcher.setNone();
        return result;
    }

    @Override
    public void write(int address, byte data) throws IOException {
        SpiSwitcher.setAPAFirst();
        super.write(address, data);
        SpiSwitcher.setNone();
    }
}
