package convertor;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import lombok.Getter;
import lombok.Setter;

public class DeviceDataConverter {
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String DATA = "DAT";
    private final String storageFolder;

    public DeviceDataConverter(String storageFolder) {
        this.storageFolder = storageFolder;
    }

    public byte[] deviceDataToBytes(DeviceData deviceData) throws Exception {
        List<byte[]> images = deviceData
                .getImages()
                .stream()
                .map(this::fileToArray)
                .collect(Collectors.toList());
        byte[] imageBundle = concatArrays(images);

        Map<String, Object> bundleMap = new HashMap<>();

        bundleMap.put("props", prepareProps(deviceData));
        bundleMap.put("sizes", prepareImageInfoHolders(deviceData, images));

        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        byte[] dataInfo = writer.writeValueAsBytes(bundleMap);

        String header = DATA + ":" + dataInfo.length + ":" + imageBundle.length + ":" + Long.BYTES + '\n';

        byte[] data = concatArrays(Arrays.asList(header.getBytes(), dataInfo, imageBundle));
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return concatArrays(Arrays.asList(data, longToBytes(crc32.getValue())));
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
        if(checkCRC(bytes)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

            String paramsString = br.readLine();
            List<Integer> params = Arrays.stream(paramsString.split(":"))
                    .map(this::tryParseInt)
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
            File file = new File(storageFolder + "_" + imageInfoHolder.getName());
            Integer size = Integer.parseInt(imageInfoHolder.getSize());
            byte[] imageBytes = new byte[size];
            System.arraycopy(bytes, offset + 1, imageBytes, 0, imageBytes.length);
            offset += size;
            saveBytesToFile(file, imageBytes);
            images.add(file);
        }
        return images;
    }

    private void saveBytesToFile(File file, byte[] imageBytes) throws IOException {
        FileOutputStream fos=new FileOutputStream(file);
        fos.write(imageBytes);
        fos.close();
    }

    private void setProps(DeviceData deviceData, Map<String, Object> bundle) {
        Map<String, String> props = (HashMap) bundle.get("props");
        deviceData.setProps(props);
    }

    private boolean checkCRC(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        byte[] crcBytes = new byte[Long.BYTES];
        System.arraycopy(bytes, bytes.length - crcBytes.length, crcBytes, 0 , crcBytes.length);
        crc32.update(bytes, 0, bytes.length - crcBytes.length);
        return bytesToLong(crcBytes) == crc32.getValue();
    }

    private Map<String, Object> getBundle(byte[] bytes, List<Integer> params, Integer offset) throws IOException {
        byte[] infoBytes = new byte[params.get(0)];
        System.arraycopy(bytes, offset + 1, infoBytes, 0, params.get(0));
        return objectMapper.readValue(infoBytes, new TypeReference<Map<String,Object>>(){});
    }

    private byte[] fileToArray(File file) {
        byte[] data = new byte[(int) file.length()];
        try {
            new FileInputStream(file).read(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private byte[] concatArrays(List<byte[]> list) {
        ByteBuffer bb = ByteBuffer.allocate(list.stream().mapToInt(image -> image.length).sum());
        list.forEach(bb::put);
        return bb.array();
    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
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

    public static void main(String[] args) throws Exception {
        DeviceDataConverter deviceDataConverter = new DeviceDataConverter("/home/minsk/IdeaProjects/Pendulum_java/src/storage/images/");
        DeviceData deviceData = new DeviceData();
        deviceData.addToImageList(Collections.singletonList(new File("/home/minsk/IdeaProjects/Pendulum_java/src/storage/images/1.jpg")));
        deviceData.addToImageList(Collections.singletonList(new File("/home/minsk/IdeaProjects/Pendulum_java/src/storage/images/2.jpg")));
        deviceData.addToImageList(Collections.singletonList(new File("/home/minsk/IdeaProjects/Pendulum_java/src/storage/images/3.jpg")));
        byte[] bytes = deviceDataConverter.deviceDataToBytes(deviceData);
        DeviceData deviceData1 = deviceDataConverter.bytesToDeviceData(bytes);
    }
}

