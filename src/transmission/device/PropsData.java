package transmission.device;

import java.io.IOException;

public interface PropsData {
    void setLedNum(Integer ledNum);

    void setBrightness(Integer percents);

    Integer getLedNum();

    Integer getBrightness();

    Integer getOffset();

    void setOffset(Integer value);

    void saveToStorage() throws IOException;
}
