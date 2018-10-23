package pendulum.display.impl;

import com.pi4j.io.spi.SpiChannel;
import pendulum.SpiSwitcher;

import java.io.IOException;

public class ImgSecondDisplayImpl extends ImgDisplayImpl {

    public ImgSecondDisplayImpl(SpiChannel spiChannel, int spiAPA102Speed, int sizeX, int ledNum) throws IOException {
        super(spiChannel, spiAPA102Speed, sizeX, ledNum);
    }

    @Override
    protected void writeStrip(int lineNum) throws IOException {
        SpiSwitcher.setAPASecond();
        super.writeStrip(lineNum);
        SpiSwitcher.setNone();
    }

    @Override
    int offsetLineNum(int lineNum) {
        return -lineNum;
    }
}