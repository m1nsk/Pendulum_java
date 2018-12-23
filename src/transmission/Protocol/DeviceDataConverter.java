package transmission.Protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import lombok.Setter;
import transmission.device.DeviceData;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class DeviceDataConverter {
    private ObjectMapper objectMapper = new ObjectMapper();
    private final File storageFolder;

    public DeviceDataConverter(File storageFolder) {
        this.storageFolder = storageFolder;
    }

    public byte[] deviceDataToBytes(DeviceData deviceData) throws JsonProcessingException {
        List<byte[]> images = deviceData
                .getImages()
                .stream()
                .map(ConvertorUtils::fileToArray)
                .collect(Collectors.toList());
        byte[] imageBundle = ConvertorUtils.concatArrays(images);

        Map<String, Object> bundleMap = new HashMap<>();

        bundleMap.put("props", prepareProps(deviceData));
        bundleMap.put("sizes", prepareImageInfoHolders(deviceData, images));

        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        byte[] dataInfo = writer.writeValueAsBytes(bundleMap);

        String header = DataType.DATA + ":" + dataInfo.length + ":" + imageBundle.length + ":" + Long.BYTES + '\n';

        byte[] data = ConvertorUtils.concatArrays(Arrays.asList(header.getBytes(), dataInfo, imageBundle));
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return ConvertorUtils.concatArrays(Arrays.asList(data, ConvertorUtils.longToBytes(crc32.getValue())));
    }

    private Map<String, String> prepareProps(DeviceData deviceData) {
        return deviceData.getProps();
    }

    private List<ImageInfoHolder> prepareImageInfoHolders(DeviceData deviceData, List<byte[]> images) {
        List<ImageInfoHolder> imageInfoHolders = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            imageInfoHolders.add(new ImageInfoHolder(String.valueOf(images.get(i).length), deviceData.getImages().get(i).getName()));
        }
        return imageInfoHolders;
    }

    public DeviceData bytesToDeviceData(byte[] bytes) throws IOException {
        DeviceData deviceData = new DeviceData();
        if(ConvertorUtils.checkCRC(bytes)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

            String paramsString = br.readLine();
            List<Integer> params = Arrays.stream(paramsString.split(":"))
                    .map(ConvertorUtils::tryParseInt)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Integer offset = paramsString.getBytes().length;

            Map<String, Object> bundle = getBundle(bytes, params, offset);
            setProps(deviceData, bundle);

            offset += params.get(0);

            List<File> images = getImages(bytes, offset, bundle);
            deviceData.setImages(images);
            return deviceData;
        }
        return null;
    }

    private List<File> getImages(byte[] bytes, Integer offset, Map<String, Object> bundle) throws IOException {
        List<File> images = new ArrayList<>();
        List<LinkedHashMap<String, String>> sizes = (ArrayList) bundle.get("sizes");
        List<ImageInfoHolder> imageInfoHolders = sizes.stream()
                .map(item -> new ImageInfoHolder(item.get("size"), item.get("name")))
                .collect(Collectors.toList());
        for (ImageInfoHolder imageInfoHolder : imageInfoHolders) {
            File file = new File(storageFolder.getPath() + "/" + imageInfoHolder.getName());
            Integer size = Integer.parseInt(imageInfoHolder.getSize());
            byte[] imageBytes = new byte[size];
            System.arraycopy(bytes, offset + 1, imageBytes, 0, imageBytes.length);
            offset += size;
            saveBytesToFile(file, imageBytes);
            images.add(file);
        }
        return images;
    }

    private static void saveBytesToFile(File file, byte[] imageBytes) throws IOException {
        FileOutputStream fos=new FileOutputStream(file);
        fos.write(imageBytes);
        fos.close();
    }

    private static void setProps(DeviceData deviceData, Map<String, Object> bundle) {
        Map<String, String> props = (HashMap) bundle.get("props");
        deviceData.setProps(props);
    }

    private Map<String, Object> getBundle(byte[] bytes, List<Integer> params, Integer offset) throws IOException {
        byte[] infoBytes = new byte[params.get(0)];
        System.arraycopy(bytes, offset + 1, infoBytes, 0, params.get(0));
        return objectMapper.readValue(infoBytes, new TypeReference<Map<String,Object>>(){});
    }

    @Getter
    @Setter
    private static class ImageInfoHolder {
        private String size;
        private String name;

        public ImageInfoHolder(String size, String name) {
            this.size = size;
            this.name = name;
        }
    }
}

