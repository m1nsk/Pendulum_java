package transmission.device;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Device implements ImageData, PropsData {
    private static final Integer LED_NUM = 5;
    private static final Integer BRIGHTNESS = 5;
    private static final Integer OFFSET = 0;
    private static volatile Device instance;
    private Storage<List<File>> imageStorage;
    private Storage<Map<String, String>> propsStorage;
    private Map<String, String> props = new HashMap<>();
    private List<File> images = new ArrayList<>();

    private Device() {
    }

    public static Device getInstance(){
        Device localInstance = instance;
        if (localInstance == null) {
            synchronized (Device.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Device();
                }
            }
        }
        if(localInstance.imageStorage != null && localInstance.propsStorage != null)
            localInstance.loadFromStorage();
        return localInstance;
    }

    public void setStorage(File file) {
        imageStorage = new StorageImageImpl(file);
        propsStorage = new StoragePropsImpl(file);
        loadFromStorage();
    }

    public void loadFromStorage() {
        try {
            images = imageStorage.loadData();
            props = propsStorage.loadData();
        } catch (IOException e) {
            images = Collections.emptyList();
            props = Collections.emptyMap();
        }
    }

    @Override
    public List<File> getImageList() {
        return images;
    }

    @Override
    public void setImageList(List<File> images) {
        this.images = images;
    }


    @Override
    public void setLedNum(Integer value) {
        props.put("ledNum", value.toString());
    }

    @Override
    public void setBrightness(Integer value) {
        if(value > 100)
            value = 100;
        if(value < 0)
            value = 0;
        props.put("brightness", value.toString());
    }

    @Override
    public Integer getLedNum() {
        return Integer.parseInt(props.getOrDefault("ledNum", LED_NUM.toString()));
    }

    @Override
    public Integer getBrightness() {
        return Integer.parseInt(props.getOrDefault("brightness", BRIGHTNESS.toString()));
    }

    @Override
    public Integer getOffset() {
        return Integer.parseInt(props.getOrDefault("offset", OFFSET.toString()));
    }

    @Override
    public void setOffset(Integer value) {
        props.put("offset", value.toString());
    }

    @Override
    public void saveToStorage() throws IOException {
        imageStorage.storeData(images);
        propsStorage.storeData(props);
    }

    @Override
    public void addToImageList(List<File> cachedImages) {
        this.images.addAll(cachedImages);
    }
}
