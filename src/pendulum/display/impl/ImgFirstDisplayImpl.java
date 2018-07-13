package pendulum.display.impl;

import pendulum.SpiSwitcher;

import java.io.IOException;

public class ImgFirstDisplayImpl extends ImgDisplayImpl {

    @Override
    protected void writeStrip(int lineNum) throws IOException {
        SpiSwitcher.setAPAFirst();
        super.writeStrip(lineNum);
        SpiSwitcher.setNone();
    }

    @Override
    int offsetLineNum(int lineNum) {
        return lineNum;
    }
}
