package pendulum.storage;

import java.util.List;

public interface ImgListStorage {

    List<byte[]> current();
    List<byte[]> next();
    List<byte[]> previous();

    void loadData();

    void chooseImgByName(String name);
}
