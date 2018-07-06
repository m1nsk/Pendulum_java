package pendulum.Impl;

import java.io.IOException;

public class ImgDisplayTwoLinesImpl extends ImgDisplayImpl {
    @Override
    protected void writeStrip(int lineNum) throws IOException {
            SpiSwitcher.setAPAFirst();
            super.writeStrip(offsetLineNum(lineNum));
            SpiSwitcher.setNone();
            SpiSwitcher.setAPAFirst();
            super.displayLine(offsetOppositeLineNum(lineNum));
            SpiSwitcher.setNone();
    }

    protected int offsetOppositeLineNum(int lineNum) {
        return sizeX / 2 - lineNum;
    }
}
