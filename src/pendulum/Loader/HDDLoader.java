package pendulum.Loader;

import pendulum.PendulumParams;
import pendulum.storage.ImgListStorage;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HDDLoader {
    private Set<String> IMAGE_EXTENSION_SET = new HashSet<>(Arrays.asList("jpeg", "jpg", "png"));
    private PendulumParams params;
    private ImgListStorage storage;
    private Long lastModified;

    public HDDLoader(PendulumParams params, ImgListStorage storage) {
        this.params = params;
    }

    public void Load() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File imageFolder = new File(classLoader.getResource(params.getImageFolderPath()).getFile());
        if(imageFolder.exists() && imageFolder.isDirectory()) {
            if (lastModified == null || !lastModified.equals(imageFolder.lastModified())) {
                lastModified = imageFolder.lastModified();
                File[] files = imageFolder.listFiles();
                Map<String, File> images = Arrays.stream(files).filter(file -> IMAGE_EXTENSION_SET.contains(getFileExtension(file)))
                        .collect(Collectors.toMap(this::getFileName, image -> image));
                storage.loadData(images);
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf('.'));
    }

    private String getFileName(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
