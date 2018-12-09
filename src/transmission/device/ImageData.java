package transmission.device;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ImageData {

    List<File> getImageList();

    void setImageList(List<File> list);

    void saveToStorage() throws IOException;

    void addToImageList(List<File> cachedImages);
}
