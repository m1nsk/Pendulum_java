package pendulum;

import com.github.dlopuch.apa102_java_rpi.Apa102Output;
import com.pi4j.io.spi.SpiMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImgDisplay {
    private Apa102Output apa102Output;
    private List<byte[]> img = new ArrayList<>();
    private PendulumParams params;

    public ImgDisplay() {

    }

    public void initDisplay(PendulumParams params) throws IOException {
        this.params = params;
        Apa102Output.initSpi(this.params.getSpiChannel(), this.params.getSpiAPA102Speed(), SpiMode.MODE_0);
        this.apa102Output = new Apa102Output(params.getLedNum());
    }

    public void displayLine(int lineNum) throws IOException {
        if (img.isEmpty() || lineNum < 0 || img.size() < lineNum)
            return;
        apa102Output.writeStrip(img.get(params.getSizeX() / 2 + lineNum));
    }

    public void setImg(List<byte[]> img) {
        this.img = img;
    }
}
