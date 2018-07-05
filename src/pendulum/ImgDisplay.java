package pendulum;

import com.github.dlopuch.apa102_java_rpi.Apa102Output;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImgDisplay {
    private Apa102Output apa102Output;
    private List<byte[]> img = new ArrayList<>();
    private int sizeX;
    public ImgDisplay() {

    }

    public void initDisplay(SpiChannel spiChannel, int spiAPA102Speed, int sizeX, int ledNum) throws IOException {
        Apa102Output.initSpi(spiChannel, spiAPA102Speed, SpiMode.MODE_0);
        this.apa102Output = new Apa102Output(ledNum);
        this.sizeX = sizeX;
    }

    public void displayLine(int lineNum) throws IOException {
        if (img.isEmpty() || lineNum < 0 || img.size() < lineNum)
            return;
        apa102Output.writeStrip(img.get(sizeX / 2 + lineNum));
    }

    public void setImg(List<byte[]> img) {
        this.img = img;
    }
}
