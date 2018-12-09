package pendulum.display.impl;

import APA102.Apa102Output;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import pendulum.display.ImgDisplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract class ImgDisplayImpl implements ImgDisplay {
    private Apa102Output apa102Output;
    private List<byte[]> img = new ArrayList<>();

    public ImgDisplayImpl(SpiChannel spiChannel, int spiAPA102Speed, int ledNum) throws IOException{
        Apa102Output.initSpi(spiChannel, spiAPA102Speed, SpiMode.MODE_0);
        this.apa102Output = new Apa102Output(ledNum);
    }

    @Override
    public void displayLine(int lineNum) throws IOException {
        if(lineNum < 0)
            lineNum = Math.abs(lineNum);
        if(lineNum >= img.size())
            lineNum = Math.abs(img.size() - lineNum);
        if (img == null || img.isEmpty())
            return;
        writeStrip(offsetLineNum(lineNum));
    }

    abstract int offsetLineNum(int lineNum);

    protected void writeStrip(int lineNum) throws IOException {
        apa102Output.writeStrip(img.get(lineNum));
    }

    @Override
    public void setImg(List<byte[]> img) {
        this.img = imgCopy(img);
    }

    private List<byte[]> imgCopy(List<byte[]> img){
        if (img == null)
            return null;
        return img.stream().map(line -> Arrays.copyOf(line, line.length)).collect(Collectors.toList());
    }
}
