package pendulum;

import java.io.File;

public class StorageFileGetter {

    private static final String PATH_TO_STORAGE = "storage/";

    public File getStorage() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File storage = new File(classLoader.getResource(PATH_TO_STORAGE).getFile());
        if (!storage.exists()) {
            storage.mkdir();
        }
        return storage;
    }
}
