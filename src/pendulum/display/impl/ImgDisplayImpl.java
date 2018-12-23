package pendulum.display.impl;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import pendulum.display.ImgDisplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract class ImgDisplayImpl implements ImgDisplay {

    private static SpiDevice spi = null;
    private List<byte[]> img = new ArrayList<>();

    public ImgDisplayImpl(SpiChannel spiChannel, int spiAPA102Speed) throws IOException{
        spi = SpiFactory.getInstance(spiChannel, spiAPA102Speed, SpiMode.MODE_0);
    }

    @Override
    public void displayLine(int lineNum) throws IOException {
        if (img == null || img.isEmpty())
            return;
        if(lineNum < 0)
            lineNum = Math.abs(lineNum);
        if(lineNum >= img.size())
            lineNum = Math.abs(img.size() - lineNum);
        writeStrip(offsetLineNum(lineNum));
    }

    abstract int offsetLineNum(int lineNum);

    protected void writeStrip(int lineNum) throws IOException {
        writeLine(img.get(lineNum));
    }

    private void writeLine(byte[] bytes) throws IOException {
        if (bytes.length < 2048) {
            spi.write(bytes);
        } else {
            int startI;
            for(startI = 0; startI <= bytes.length; startI += 2048) {
                spi.write(bytes, startI, 2048);
            }

        }
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
