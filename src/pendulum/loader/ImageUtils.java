package pendulum.loader;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ImageUtils {
    public static final Set<String> IMAGE_EXTENSION_SET = new HashSet<>(Arrays.asList("jpeg", "jpg", "png"));

    public static String getFileExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static String getFileName(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
