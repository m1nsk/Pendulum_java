package pendulum.Loader;

import pendulum.PendulumParams;
import pendulum.storage.ImgListStorage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HDDLoader {
    private PendulumParams params = PendulumParams.getInstance();
    private ImgListStorage storage;
    private Long lastModified;

    public HDDLoader(ImgListStorage storage) {
        this.storage = storage;
    }

    public void Load() throws IOException {
        File imageFolder = params.getImagesStorageFolder();
        if(imageFolder.exists() && imageFolder.isDirectory()) {
            if (lastModified == null || !lastModified.equals(imageFolder.lastModified())) {
                lastModified = imageFolder.lastModified();
                File[] files = imageFolder.listFiles();
                Map<String, File> images = Arrays.stream(files).filter(file -> ImageUtils.IMAGE_EXTENSION_SET.contains(ImageUtils.getFileExtension(file)))
                        .collect(Collectors.toMap(ImageUtils::getFileName, image -> image));
                storage.loadData();
            }
        }
    }

}
