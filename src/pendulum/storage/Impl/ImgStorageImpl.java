package pendulum.storage.Impl;

import pendulum.storage.ImgStorage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

public class ImgStorageImpl implements ImgStorage {
    private Map<String, List<byte[]>> imgMap;
    private Map<String, List<byte[]>> imgMapBuffer;
    private List<String> instructions;
    private int xSize;
    private int ySize;

    public ImgStorageImpl(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
        imgMap = new HashMap<>();
        imgMapBuffer = new HashMap<>();
        instructions = new ArrayList<>();
    }

    private boolean isBufferEmpty() {
        return imgMapBuffer.isEmpty();
    }

    @Override
    public synchronized List<byte[]> getImg(String name) {
        if (!isBufferEmpty()) {
            imgMap = imgMapBuffer;
            imgMapBuffer.clear();
        }
        List<byte[]> img = copyImg(name);
        return img;
    }

    @Override
    public Map<String, List<byte[]>> getImgMap() {
        return imgMap;
    }

    @Override
    public List<String> getInstructions() {
        return instructions;
    }

    @Override
    public synchronized void loadData(Map<String, File> imgMap, List<String> instructions) throws IOException {
        this.instructions = instructions;
        Set<Map.Entry<String, File>> entries = imgMap.entrySet();
        for (Map.Entry<String, File> entry : entries) {
            this.imgMapBuffer.put(entry.getKey(), convertImage(entry.getValue()));
        }
    }

    private List<byte[]> copyImg(String name) {
        List<byte[]> img = new ArrayList<>();
        imgMap.getOrDefault(name, new ArrayList<>()).stream().forEach(item -> {
            byte[] line = new byte[item.length];
            System.arraycopy(item, 0, line, 0, item.length);
            img.add(line);
        });
        return img;
    }

    private List<byte[]> convertImage(File file) throws IOException {
        List<byte[]> result = new ArrayList<>();

        BufferedImage bImg = ImageIO.read(file);    //read img from file
        bImg = resizeImg(bImg);
        //TODO: make polar coord conversion
        for (int a = 0; a < xSize; a++) { //covert image to byteArray list
            byte[] line = new byte[ySize * 4];
            for (int l = 0; l < ySize; l++) {
                int rgb = polarConverter(a, l, bImg);
//                int rgb = bImg.getRGB(i, j);
                byte[] bytes = ByteBuffer.allocate(4).putInt(rgb).array();
                for (int k = 0; k < bytes.length; k++) {
                    line[l * 4] = bytes[k];
                }
            }
            result.add(line);
        }
        return result;
    }

    private BufferedImage resizeImg(BufferedImage bImg) {
        Image tmp = bImg.getScaledInstance(xSize, ySize, Image.SCALE_SMOOTH);   //scale image
        bImg = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return bImg;
    }

    private int polarConverter(int a, int l, BufferedImage bImg) {
        int DELTA_X = xSize / 2;
        int BLACK_PIXEL = 1111 << 16; // 1111 0000 0000 0000
        int x = (int) (DELTA_X - l * Math.cos(Math.toRadians(a / xSize * 180)));
        if (x < 0 || x >= xSize) {
            return BLACK_PIXEL;
        } else {
            int y = (int) (l * Math.sin(Math.toRadians(a / xSize * 180)));
            if (y < 0 || y >= ySize)
                return BLACK_PIXEL;
            return bImg.getRGB(x, y);
        }
    }
}
