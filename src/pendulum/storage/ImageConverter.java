package pendulum.storage;

import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ImageConverter {
    private final static int BLACK_PIXEL = -16777216;
    private final static int[] LED_GAMMA = {
            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  1,  1,  1,
            1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  2,  2,  2,  2,  2,  2,
            2,  3,  3,  3,  3,  3,  3,  3,  4,  4,  4,  4,  4,  5,  5,  5,
            5,  6,  6,  6,  6,  7,  7,  7,  7,  8,  8,  8,  9,  9,  9, 10,
            10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 15, 15, 16, 16,
            17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 24, 24, 25,
            25, 26, 27, 27, 28, 29, 29, 30, 31, 32, 32, 33, 34, 35, 35, 36,
            37, 38, 39, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 50,
            51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 66, 67, 68,
            69, 70, 72, 73, 74, 75, 77, 78, 79, 81, 82, 83, 85, 86, 87, 89,
            90, 92, 93, 95, 96, 98, 99,101,102,104,105,107,109,110,112,114,
            115,117,119,120,122,124,126,127,129,131,133,135,137,138,140,142,
            144,146,148,150,152,154,156,158,160,162,164,167,169,171,173,175,
            177,180,182,184,186,189,191,193,196,198,200,203,205,208,210,213,
            215,218,220,223,225,228,231,233,236,239,241,244,247,249,252,255 };
    private int xSize;
    private int ySize;
    private int offset;
    private int polarYSize;
    private double brightness;

    public ImageConverter(int polarYSize) {
        this.polarYSize = polarYSize;
    }

    public List<byte[]> convertImage(File file) throws IOException {
        List<byte[]> result = new ArrayList<>();
        brightness = 0.2;
        BufferedImage bImg = ImageIO.read(file);    //read img from file
        bImg = resizeImg(bImg);
        byte swap;
        for (int a = 0; a < polarYSize; a++) { //covert images to byteArray list
            byte[] line = new byte[ySize * 4];
            for (int l = 0; l < ySize; l++) {
                int rgb = polarConverter(a, l, bImg);
                byte[] bytes = ByteBuffer.allocate(4).putInt(rgb).array();
                if(brightness != 1.0) {
                    swap = bytes[1];
                    bytes[1] = (byte)(LED_GAMMA[(int)((bytes[3] + 255) % 255 * brightness)]);
                    bytes[2] = (byte)(LED_GAMMA[(int)((bytes[2] + 255) % 255 * brightness)]);
                    bytes[3] = (byte)(LED_GAMMA[(int)((swap + 255) % 255 * brightness)]);
                }
                System.arraycopy(bytes, 0, line, l * 4, 4);
            }
            result.add(line);
        }
        return result;
    }

    private BufferedImage resizeImg(BufferedImage bImg) {
        Image tmp = bImg.getScaledInstance(xSize, ySize, Image.SCALE_FAST);   //scale images
        bImg = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return bImg;
    }

    private int polarConverter(int a, int l, BufferedImage bImg) {
        l = l + offset;
        int DELTA_X = xSize / 2;
        int x = (int) (DELTA_X - l * Math.cos(Math.toRadians((float)a / polarYSize * 180)));
        if (x < 0 || x >= xSize) {
            return BLACK_PIXEL;
        } else {
            int y = (int) (l * Math.sin(Math.toRadians((float)a / polarYSize * 180)) - offset);
            if (y < 0 || y >= ySize)
                return BLACK_PIXEL;
            return bImg.getRGB(x, bImg.getHeight() - y - 1);
        }
    }
}
