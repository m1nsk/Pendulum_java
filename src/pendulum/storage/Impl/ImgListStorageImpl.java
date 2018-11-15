package pendulum.storage.Impl;

import pendulum.storage.ImgListStorage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ImgListStorageImpl implements ImgListStorage {
    private List<List<byte[]>> imgListBuffer;
    private int xSize;
    private int ySize;
    private int pointer = 0;

    public ImgListStorageImpl(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
        imgListBuffer = new ArrayList<>();
    }

    private boolean isBufferEmpty() {
        return imgListBuffer.isEmpty();
    }

    @Override
    public synchronized List<byte[]> current() {
        if(imgListBuffer.isEmpty())
            return null;
        return imgListBuffer.get(pointer);
    }

    @Override
    public List<byte[]> next() {
        if(imgListBuffer.isEmpty())
            return current();
        pointer = ++pointer % imgListBuffer.size();
        return current();
    }

    @Override
    public List<byte[]> previous() {
        if(imgListBuffer.isEmpty())
            return current();
        pointer = (--pointer + imgListBuffer.size()) % imgListBuffer.size();
        return current();
    }


    @Override
    public synchronized void loadData(Map<String, File> imgMap) throws IOException {
        imgListBuffer = imgMap.entrySet().stream()
                .sorted((e1, e2) -> {
                    Integer key1 = tryParseInt(e1.getKey());
                    Integer key2 = tryParseInt(e2.getKey());
                    if(key1 == null || key2 == null)
                        return -1;
                    return key1.compareTo(key2);
                }).filter(entry -> entry.getKey() != null).map(entry -> {
                    try {
                        return convertImage(entry.getValue());
                    } catch (IOException e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Integer tryParseInt(String value) {
        try{
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<byte[]> convertImage(File file) throws IOException {
        List<byte[]> result = new ArrayList<>();

        BufferedImage bImg = ImageIO.read(file);    //read img from file
        bImg = resizeImg(bImg);
        //TODO: make polar coord conversion
        for (int a = 0; a < xSize; a++) { //covert images to byteArray list
            byte[] line = new byte[ySize * 3];
            for (int l = 0; l < ySize; l++) {
                int rgb = polarConverter(a, l, bImg);
//                int rgb = bImg.getRGB(i, j);
                byte[] bytes = ByteBuffer.allocate(4).putInt(rgb).array();
                for (int i = 1; i < bytes.length; i++) {
                    line[l * 3] = bytes[i];
                }
            }
            result.add(line);
        }
        return result;
    }

    private BufferedImage resizeImg(BufferedImage bImg) {
        Image tmp = bImg.getScaledInstance(xSize, ySize, Image.SCALE_SMOOTH);   //scale images
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
            return bImg.getRGB(x, y) ;
        }
    }
}
