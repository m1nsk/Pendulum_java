package pendulum;

import com.pi4j.io.spi.SpiChannel;

import java.io.IOException;
import java.util.List;

public interface ImgDisplay {

    void initDisplay(SpiChannel spiChannel, int spiAPA102Speed, int sizeX, int ledNum) throws IOException;

    void displayLine(int lineNum) throws IOException;

    void setImg(List<byte[]> img);

}
