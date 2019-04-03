package pendulum.storage.Impl;

import pendulum.storage.ImageConverter;
import pendulum.storage.ImgListStorage;
import transmission.device.Device;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ImgListStorageImpl implements ImgListStorage {
    private Device device = Device.getInstance();
    private Map<String, Integer> names = new HashMap<>();
    private List<List<byte[]>> imgListBuffer = new ArrayList<>();
    private ImageConverter imageConverter;
    private int pointer = 0;

    public ImgListStorageImpl(int polarYSize) {
        imageConverter = new ImageConverter(polarYSize);
        loadData();
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
        imageConverter.setYSize(ledNum * 144);
        imageConverter.setXSize(ledNum * 2 * 144);
        imageConverter.setBrightness(device.getBrightness());
        imageConverter.setOffset(device.getOffset());
        device.loadFromStorage();
        List<File> images = device.getImageList();
        imgListBuffer.clear();
        names.clear();
        for (int i = 0; i < images.size(); i++) {
            try {
                imgListBuffer.add(imageConverter.convertImage(images.get(i)));
                names.put(images.get(i).getName(), i);
            } catch (IOException ignored) {
            }
        }
        this.pointer = 0;
    }

    @Override
    public void chooseImgByName(String name) {
        this.pointer = names.get(name);
    }


}
