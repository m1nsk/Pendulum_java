package pendulum.display;

import java.io.IOException;
import java.util.List;

public interface ImgDisplay {

    void displayLine(Double lineNum) throws IOException;

    void setImg(List<byte[]> img);

}
