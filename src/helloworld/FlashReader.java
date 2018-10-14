package helloworld;

import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbHub;
import java.util.List;

public class FlashReader {
    private List<String> filePorts;
    private String fileStorage;

    public FlashReader(List<String> filePorts, String fileStorage) {
        this.filePorts = filePorts;
        this.fileStorage = fileStorage;
    }

    public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }
}
