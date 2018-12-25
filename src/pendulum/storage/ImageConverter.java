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
    private int xSize;
    private int ySize;
    private int polarYSize;
    private double brightness;
    private final int BLACK_PIXEL = 0b1111 << 12;

    public ImageConverter(int polarYSize) {
        this.polarYSize = polarYSize;
    }

    public List<byte[]> convertImage(File file) throws IOException {
        List<byte[]> result = new ArrayList<>();

        BufferedImage bImg = ImageIO.read(file);    //read img from file
        bImg = resizeImg(bImg);
        for (int a = 0; a < polarYSize; a++) { //covert images to byteArray list
            byte[] line = new byte[ySize * 4];
            for (int l = 0; l < ySize; l++) {
                int rgb = polarConverter(a, l, bImg);
                byte[] bytes = ByteBuffer.allocate(4).putInt(rgb).array();
                if(brightness != 10.0) {
                    bytes[1] = (byte)((int)bytes[1] * brightness);
                    bytes[2] = (byte)((int)bytes[2] * brightness);
                    bytes[3] = (byte)((int)bytes[3] * brightness);
                }
                System.arraycopy(bytes, 0, line, l * 4, 4);
            }
            result.add(line);
        }
        return result;
    }

    private BufferedImage resizeImg(BufferedImage bImg) {
        Image tmp = bImg.getScaledInstance(xSize, ySize, Image.SCALE_SMOOTH);   //scale images
        bImg = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return bImg;
    }

    private int polarConverter(int a, int l, BufferedImage bImg) {
        int DELTA_X = xSize / 2; // 1111 0000 0000 0000
        int x = (int) (DELTA_X - l * Math.cos(Math.toRadians((float)a / polarYSize * 180)));
        if (x < 0 || x >= xSize) {
            return BLACK_PIXEL;
        } else {
            int y = (int) (l * Math.sin(Math.toRadians((float)a / polarYSize * 180)));
            if (y < 0 || y >= ySize)
                return BLACK_PIXEL;
            return bImg.getRGB(x, y);
        }
    }
}
