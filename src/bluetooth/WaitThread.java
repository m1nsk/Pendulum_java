package bluetooth;

import lombok.extern.slf4j.Slf4j;
import observer.EventManager;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;

@Slf4j(topic = "WAIT THREAD")
public class WaitThread implements Runnable{

	private static EventManager eventManager;

	public WaitThread(EventManager eventManager) {
		WaitThread.eventManager = eventManager;
	}
	
	@Override
	public void run() {
		waitForConnection();		
	}
	
	/** Waiting for connection from devices */
	private void waitForConnection() {
		// retrieve the local Bluetooth device object
		LocalDevice local = null;
		
		StreamConnectionNotifier notifier;
		StreamConnection connection = null;
		
		// setup the server to listen for connection
		try {
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);
			
			UUID uuid = new UUID("04c6093b00001000800000805f9b34fb", false);
			log.info(uuid.toString());
			
            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
            notifier = (StreamConnectionNotifier)Connector.open(url);
        } catch (BluetoothStateException e) {
        	log.error("Bluetooth is not turned on.");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// waiting for connection
		while(true) {
			try {
				log.info("waiting for connection...");
	            connection = notifier.acceptAndOpen();
	            Thread processThread = new Thread(new ProcessConnectionThread(connection, eventManager));
	            processThread.start();
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
				return;
			}
		}
	}
}
