package bluetooth;

import Observer.EventManager;
import Observer.EventType;
import transmission.Protocol.BundleInfo;
import transmission.Protocol.DataType;
import transmission.Protocol.DeviceDataConverter;
import transmission.device.Device;
import transmission.device.DeviceData;

import javax.microedition.io.StreamConnection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessConnectionThread implements Runnable{

    private StreamConnection mConnection;

    private EventManager eventManager;

    private File storage = new File("/home/minsk/IdeaProjects/Pendulum_java/src/storage/images/");

    private DeviceDataConverter deviceDataConverter;

    private Device device = Device.getInstance();

    public ProcessConnectionThread(StreamConnection connection, EventManager eventManager)
    {
        this.eventManager = eventManager;
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
                eventManager.notify(EventType.STORAGE_UPDATED);
            } else if(bundleInfo.getType().equals(DataType.COMMAND)) {
                return;
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
}
