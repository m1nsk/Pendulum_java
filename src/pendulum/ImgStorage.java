package pendulum;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ImgStorage {

    List<byte[]> getImg(String name);

    Map<String, List<byte[]>> getImgMap();

    List<String> getInstructions();

    void loadData(Map<String, File> imgMap, List<String> instructions) throws IOException;
}
