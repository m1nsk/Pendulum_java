package bluetooth;

import observer.EventManager;
import observer.EventType;
import pendulum.StorageFileGetter;
import transmission.Protocol.*;
import transmission.device.Device;
import transmission.device.DeviceData;

import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessConnectionThread implements Runnable{

    private StreamConnection mConnection;

    private EventManager eventManager;

    private DeviceDataConverter deviceDataConverter;

    private DeviceCommandConverter deviceCommandConverter;

    private Device device = Device.getInstance();

    public ProcessConnectionThread(StreamConnection connection, EventManager eventManager)
    {  this.eventManager = eventManager;
        mConnection = connection;
        deviceDataConverter = new DeviceDataConverter(new StorageFileGetter().getStorage());
        deviceCommandConverter = new DeviceCommandConverter();
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
                    bundleInfo = DataTypeHelper.getBundleInfo(buffer);
                    bytes = new byte[bundleInfo.getFullSize()];
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
                Command command = deviceCommandConverter.bytesToCommand(bytes);
                CommandQueue.offer(command);
                eventManager.notify(EventType.MESSAGE_RECEIVE);
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
