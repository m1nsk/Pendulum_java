package pendulum.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ImgListStorage {

    List<byte[]> current();
    List<byte[]> next();
    List<byte[]> previous();

    void loadData(Map<String, File> imgMap) throws IOException;
}
