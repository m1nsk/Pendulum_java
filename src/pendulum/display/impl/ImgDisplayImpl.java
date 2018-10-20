package pendulum.display.impl;

import com.github.dlopuch.apa102_java_rpi.Apa102Output;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import pendulum.display.ImgDisplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class ImgDisplayImpl implements ImgDisplay {
    private Apa102Output apa102Output;
    private List<byte[]> img = new ArrayList<>();
    private int sizeX;

    public ImgDisplayImpl(SpiChannel spiChannel, int spiAPA102Speed, int sizeX, int ledNum) throws IOException{
        Apa102Output.initSpi(spiChannel, spiAPA102Speed, SpiMode.MODE_0);
        this.apa102Output = new Apa102Output(ledNum);
        this.sizeX = sizeX;
    }

    @Override
    public void displayLine(int lineNum) throws IOException {
        System.out.println(lineNum);
        if (img.isEmpty() || lineNum < 0 || img.size() < lineNum)
            return;
        writeStrip(offsetLineNum(lineNum));
    }

    abstract int offsetLineNum(int lineNum);// {return sizeX / 2 + lineNum;}

    protected void writeStrip(int lineNum) throws IOException {
        apa102Output.writeStrip(img.get(lineNum));
    }

    @Override
    public void setImg(List<byte[]> img) {
        this.img = img;
    }
}
