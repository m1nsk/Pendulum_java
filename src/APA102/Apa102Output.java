package APA102;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import java.io.IOException;

public class Apa102Output {
    private static final int BYTES_PER_LED = 4;
    private static byte[] START_FRAME = new byte[]{0, 0, 0, 0};
    private static SpiDevice spi = null;
    private byte[] ledBuffer;
    private int i_firstLedFrame;
    private int i_endFrame;
    private Apa102Output.ColorOrder colorOrder;

    public static void initSpi() throws IOException {
        initSpi(SpiChannel.CS0, 15700000, SpiDevice.DEFAULT_SPI_MODE);
    }

    public static void initSpi(SpiChannel spiChannel, int spiSpeed, SpiMode spiMode) throws IOException {
        spi = SpiFactory.getInstance(spiChannel, spiSpeed, spiMode);
    }

    public Apa102Output() {
        this(ColorConfig.BGR);
    }

    public Apa102Output(Apa102Output.ColorOrder colorConfig) {
        if (spi == null) {
            throw new RuntimeException("Call .initSpi() before constructing new output!");
        } else {
            this.colorOrder = colorConfig;
            this.numLeds = numLeds;
            this.i_firstLedFrame = 4;
            this.i_endFrame = this.i_firstLedFrame + numLeds * 4;
            this.ledBuffer = new byte[4 + numLeds * 4 + (int)Math.ceil((double)numLeds / 2.0D / 8.0D)];

            int i;
            for(i = this.i_firstLedFrame; i < this.i_endFrame; i += 4) {
                this.ledBuffer[i] = -1;
            }

            for(i = this.i_endFrame; i < this.ledBuffer.length; ++i) {
                this.ledBuffer[i] = -1;
            }

        }
    }

    public void writeStrip(byte[] rgbTriplets) throws IOException {
        if (rgbTriplets.length != this.numLeds * 3) {
            throw new RuntimeException("Invalid rgbTriplets.  Expected " + this.numLeds + " LEDs, or length " + this.numLeds * 3);
        } else {
            int colorI = 0;

            int startI;
            for(startI = this.i_firstLedFrame; startI < this.i_endFrame; startI += 4) {
                this.ledBuffer[startI + 1 + this.colorOrder.getRed()] = rgbTriplets[colorI++];
                this.ledBuffer[startI + 1 + this.colorOrder.getGreen()] = rgbTriplets[colorI++];
                this.ledBuffer[startI + 1 + this.colorOrder.getBlue()] = rgbTriplets[colorI++];
            }

            if (this.ledBuffer.length < 2048) {
                spi.write(this.ledBuffer);
            } else {
                for(startI = 0; startI <= this.ledBuffer.length; startI += 2048) {
                    spi.write(this.ledBuffer, startI, 2048);
                }

            }
        }
    }

    public static enum ColorConfig implements Apa102Output.ColorOrder {
        BGR(2, 1, 0),
        RGB(0, 1, 2);

        private int r;
        private int g;
        private int b;

        private ColorConfig(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int getRed() {
            return this.r;
        }

        public int getGreen() {
            return this.g;
        }

        public int getBlue() {
            return this.b;
        }
    }

    public interface ColorOrder {
        int getRed();

        int getGreen();

        int getBlue();
    }
}
