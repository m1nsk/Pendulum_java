package transmission.device;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StorageImageImpl implements Storage<List<File>> {
    private final String DEVICE = "device";
    private final File imgDir;
    private ObjectMapper objectMapper = new ObjectMapper();

    public StorageImageImpl(File storage) {
        imgDir = new File(storage.getPath() + "/" + DEVICE);
        if(!imgDir.exists()) {
            imgDir.mkdir();
        }
    }

    @Override
    public List<File> loadData() throws IOException {
        return loadImages();
    }

    private List<File> loadImages() throws IOException {
        File imagesFile = new File(imgDir + "/images.txt");
        List<String> imageList = new ArrayList<>(Arrays.asList(objectMapper.readValue(imagesFile, String[].class)));
        List<File> images = imageList.stream().map(File::new).collect(Collectors.toList());
        return images;
    }

    @Override
    public void storeData(List<File> data) throws IOException {
        saveImages(data);
    }

    private void saveImages(List<File> images) throws IOException {
        for (File item : images) {
            File file = new File(imgDir + "/" + item.getName());
            copy(item, file);
        }
        List<String> paths = images.stream().map(File::getPath).collect(Collectors.toList());
        File file = new File(imgDir + "/images.txt");
        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(file, paths);
    }

    private void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
}
