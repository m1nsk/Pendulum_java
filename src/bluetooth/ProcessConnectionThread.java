package bluetooth;

import transmission.Protocol.BundleInfo;
import transmission.Protocol.DataType;
import transmission.Protocol.DeviceDataConverter;
import transmission.device.Device;
import transmission.device.DeviceData;

import javax.imageio.ImageIO;
import javax.microedition.io.StreamConnection;
import java.awt.image.BufferedImage;
import java.io.*;

public class ProcessConnectionThread implements Runnable{

    private StreamConnection mConnection;

    private File storage = new File("/home/minsk/IdeaProjects/Pendulum_java/src/storage/images/");

    private DeviceDataConverter deviceDataConverter;

    private Device device = Device.getInstance();

    public ProcessConnectionThread(StreamConnection connection)
    {
        mConnection = connection;
        if(!storage.exists()) {
            storage.mkdir();
        }
        deviceDataConverter = new DeviceDataConverter(storage.getPath());
        device.setStorage(storage);
    }

    @Override
    public void run() {
        try {

            // prepare to receive data
            InputStream inputStream = mConnection.openInputStream();
            OutputStream outputStream = mConnection.openOutputStream();

            System.out.println("waiting for input");

            boolean bytesFlag = false;
            byte[] bytes = new byte[2048];
            int counter = 0;
            byte[] buffer = new byte[2048];

            DataType dataType;
            BundleInfo bundleInfo = null;

            while (counter < bytes.length) {
                int command = inputStream.read(buffer);
                if(!bytesFlag) {
                    bundleInfo = deviceDataConverter.getBundleInfo(buffer);
                    bytes = new byte[bundleInfo.getBundleSize()];
                    bytesFlag = true;
                }
                System.arraycopy(buffer, 0, bytes, counter, command);
                counter += command;
            }
            if(bundleInfo.getType().equals(DataType.DATA)) {
                DeviceData deviceData = deviceDataConverter.bytesToDeviceData(bytes);
                saveDeviceDataToStorage(deviceData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDeviceDataToStorage(DeviceData deviceData) throws IOException {
        device.setImageList(deviceData.getImages());
        device.setLedNum(deviceData.getLedNum());
        device.setBrightness(deviceData.getBrightness());
        device.saveToStorage();
    }

    private BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
