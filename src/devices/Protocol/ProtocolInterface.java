package devices.Protocol;

import java.io.IOException;

/**
 *
 * @author minsk
 */
public interface ProtocolInterface
{

    byte read(int address) throws IOException;
    byte[] read(int address, int count) throws IOException;

    void write(int address, byte data) throws IOException;

}
