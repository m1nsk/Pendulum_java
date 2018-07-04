package pendulum;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

public class ImgStorage {
    private Map<String,List<byte[]>> imgMap;
    private Map<String,List<byte[]>> imgMapBuffer;
    private String instructions;
    private int xSize;
    private int ySize;

    public ImgStorage(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
        imgMap = new HashMap<>();
        imgMapBuffer = new HashMap<>();
        instructions = "";
    }

    private boolean isBufferEmpty() {
        return imgMapBuffer.isEmpty();
    }

    public synchronized List<byte[]> getImg(String name) {
        if (!isBufferEmpty()) {
            imgMap = imgMapBuffer;
            imgMapBuffer.clear();
        }
        List<byte[]> img = copyImg(name);
        return img;
    }

    private List<byte[]> copyImg(String name) {
        List<byte[]> img = new ArrayList<>();
        imgMap.get(name).stream().forEach(item -> {
            byte[] line = new byte[item.length];
            System.arraycopy( item, 0, line, 0, item.length );
            img.add(line);
        });
        return img;
    }

    public synchronized void setImgMapBuffer(Map<String, File> imgMap) throws IOException {
        Set<Map.Entry<String, File>> entries = imgMap.entrySet();
        for (Map.Entry<String, File> entry : entries) {
            this.imgMapBuffer.put(entry.getKey(), convertImage(entry.getValue()));
        }
    }

    private List<byte[]> convertImage(File file) throws IOException {
        List<byte[]> result = new ArrayList<>();

        BufferedImage bImg = ImageIO.read(file);    //read img from file
        Image tmp = bImg.getScaledInstance(xSize, ySize, Image.SCALE_SMOOTH);   //scale image
        bImg = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        for (int i = 0; i < xSize; i++) { //covert image to byteArray list
            byte[] line = new byte[ySize * 4];
            for (int j = 0; j < ySize; j++) {
                int rgb = bImg.getRGB(i, j);
                byte[] bytes = ByteBuffer.allocate(4).putInt(1695609641).array();
                for (int k = 0; k < bytes.length; k++) {
                    line[j * 4] = bytes[k];
                }
            }
            result.add(line);
        }
        return result;
    }
}
