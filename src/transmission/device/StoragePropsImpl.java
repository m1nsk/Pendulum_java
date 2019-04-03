package transmission.device;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StoragePropsImpl implements Storage<Map<String, String>> {
    private final String DEVICE = "device";
    private final File imgDir;
    private ObjectMapper objectMapper = new ObjectMapper();

    public StoragePropsImpl(File storage) {
        imgDir = new File(storage.getPath() + "/" + DEVICE);
        if(!imgDir.exists()) {
            imgDir.mkdir();
        }
    }

    @Override
    public Map<String, String> loadData() throws IOException {
        return loadProps();
    }

    private Map<String, String> loadProps() throws IOException {
        File propsFile = new File(imgDir + "/props.txt");
        if(propsFile.exists()) {
            Map<String, String> props = objectMapper.readValue(propsFile, new TypeReference<Map<String, String>>() {});
            return props;
        }
        return new HashMap<>();
    }

    @Override
    public void storeData(Map<String, String> data) throws IOException {
        saveProps(data);
    }

    private void saveProps(Map<String, String> props) throws IOException {
        File file = new File(imgDir + "/props.txt");
        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(file, props);
    }
}
