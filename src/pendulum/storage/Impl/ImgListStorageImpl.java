package pendulum.storage.Impl;

import pendulum.storage.ImgListStorage;
import transmission.device.Device;

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
    private Device device = Device.getInstance();
    private List<List<byte[]>> imgListBuffer = new ArrayList<>();
    private int xSize;
    private int ySize;
    private int pointer = 0;
    private int polarYSize;

    public ImgListStorageImpl(int polarYSize) {
        this.polarYSize = polarYSize;
        loadData();
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
    public synchronized List<byte[]> next() {
        if(imgListBuffer.isEmpty())
            return current();
        pointer = ++pointer % imgListBuffer.size();
        return current();
    }

    @Override
    public synchronized List<byte[]> previous() {
        if(imgListBuffer.isEmpty())
            return current();
        pointer = (--pointer + imgListBuffer.size()) % imgListBuffer.size();
        return current();
    }

    @Override
    public synchronized void loadData() {
        Integer ledNum = device.getLedNum();
        this.ySize = ledNum * 144;
        this.xSize = ledNum * 2 * 144;
        device.loadFromStorage();
        List<File> images = device.getImageList();
        imgListBuffer = images.stream().map(img -> {
                    try {
                        return convertImage(img);
                    } catch (IOException e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.pointer = 0;
    }

    private List<byte[]> convertImage(File file) throws IOException {
        List<byte[]> result = new ArrayList<>();

        BufferedImage bImg = ImageIO.read(file);    //read img from file
        bImg = resizeImg(bImg);
        //TODO: make polar coord conversion
        for (int a = 0; a < polarYSize; a++) { //covert images to byteArray list
            byte[] line = new byte[ySize * 3];
            for (int l = 0; l < ySize; l++) {
                int rgb = polarConverter(a, l, bImg);
                byte[] bytes = ByteBuffer.allocate(4).putInt(rgb).array();
                System.arraycopy(bytes, 1, line, l * 3, 3);

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
        int DELTA_X = xSize / 2;
        int BLACK_PIXEL = 1111 << 16; // 1111 0000 0000 0000
        int x = (int) (DELTA_X - l * Math.cos(Math.toRadians((float)a / polarYSize * 180)));
        if (x < 0 || x >= xSize) {
            return BLACK_PIXEL;
        } else {
            int y = (int) (l * Math.sin(Math.toRadians((float)a / polarYSize * 180)));
            if (y < 0 || y >= ySize)
                return BLACK_PIXEL;
            return bImg.getRGB(x, y) ;
        }
    }
}
