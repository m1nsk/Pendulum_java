package pendulum.display.impl;

import com.pi4j.io.spi.SpiChannel;

import java.io.IOException;

public class ImgDefaultDisplayImpl extends ImgDisplayImpl {

    public ImgDefaultDisplayImpl(SpiChannel spiChannel, int spiAPA102Speed) throws IOException {
        super(spiChannel, spiAPA102Speed);
    }

    @Override
    protected void writeStrip(int lineNum) throws IOException {
        super.writeStrip(lineNum);
    }

    @Override
    int offsetLineNum(int lineNum) {
        return lineNum;
    }
}
