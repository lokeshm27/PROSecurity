import java.io.IOException;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class BTOperations {
    public final static long OBEX_OBJECT_PUSH = 0x1105;
    public final static long OBEX_FILE_TRANSFER = 0x1106;
    public final static long HANDS_FREE = 0x111E;
    public final static long SDP = 0x001;
    public final static long RFCOMM = 0x0003;
    public final static long OBEX = 0x0008;
    public final static long HTTP = 0x000C;
    public final static long SERIAL_PORT = 0x1101;
    public final static long PAN_USER = 0x1115;
    public final static long NETWORK_ACCESS_POINT = 0x1116;
    public final static long GROUP_NETWORK = 0x1117;
    public final static String loggerName = "default.runtime";
	
	static boolean dontShow = false;
	static int responseCode;

	static Logger logger;
	
	/**
	 *  Initializes logger
	 */
	public static void init() {
		logger = Logger.getLogger(loggerName);
		logger.info("BTOperations Initialized");
	}
	
	/**
	 * Checks if bluetooth is turned on
	 * 
	 * @param useRecurssion If true, Recursively checks whether is device on untill
	 * 'cancel' button is clicked
	 * @return true, if bluetooth is on else false
	 */
	public static boolean isPowerOn(boolean useRecurssion) {
		if (useRecurssion) {
			boolean retry = true;
			while (retry && !LocalDevice.isPowerOn()) {
				retry = SOptions.showConfirm(null, "Bluetooth Search failed - PROSecurity",
						"Failed to search for Bluetooth devices as Bluetooth is turned off. Please turn On the Bluetooth and Click 'OK' to retry.");
			}

			if (!LocalDevice.isPowerOn()) {
				dontShow = false;
				return false;
			}
			return LocalDevice.isPowerOn();
		} else {
			if (!LocalDevice.isPowerOn()) {
				if (!dontShow) {
					SOptions.showInformation(null, "Bluetooth turnded off - PROSecurity",
							"As Bluetooth turned off, some functions off PROSecurity may not work"
									+ " untill its turned on.");
					dontShow = true;
				}
			}
			return LocalDevice.isPowerOn();
		}
	}

	/**
	 * Returns all Bluetooth devices paired with the current computer. Check for
	 * isPowerOn() before calling this function
	 * 
	 * @return Array of bluetooth devices, null if device is powered off
	 */
	public static RemoteDevice[] getPairedDevices() {
		if (!isPowerOn(false)) {
			return null;
		}
		try {
			return LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
		} catch (BluetoothStateException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return array of UUIDs supported by our system
	 * 
	 * @return Array of UUIDs
	 */
	public static long[] getUUIDs() {
		long[] uuids = new long[] {
				OBEX, OBEX_FILE_TRANSFER, OBEX_OBJECT_PUSH, SDP, HANDS_FREE, RFCOMM,
				HTTP, SERIAL_PORT, PAN_USER, NETWORK_ACCESS_POINT, GROUP_NETWORK
		};
		return uuids;
	}
	
	/**
	 * Checks range of the specified bluetooth device using handshake mechanism
	 * 
	 * @param device Bluetooth device whose range has to be checked
	 * @param service UUID of the service to be considered
	 * 
	 * @return true: If found in the range else false
	 */
	public static boolean checkRange(RemoteDevice device, UUID service) {
		if (!isPowerOn(false)) {
			return false;
		} else {
			try {
				logger.info("Checking range of the bluetooth device: " + device.getFriendlyName(false) + " MAC: " + device.getBluetoothAddress());
				int[] attributes = new int[] { 0x0100, 0x0000 };
				Object syncObject = new Object();
				
				DiscoveryAgent agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
				agent.searchServices(attributes, new UUID[] { service }, device, new DiscoveryListener() {

					@Override
					public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
						// DO Nothing
					}

					@Override
					public void inquiryCompleted(int arg0) {
						// DO Nothing
					}

					@Override
					public void serviceSearchCompleted(int arg0, int arg1) {

						synchronized (syncObject) {
							responseCode = arg1;
							syncObject.notify();
						}
					}

					@Override
					public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
						// TODO Add logging statements
					}

				});

				synchronized (syncObject) {
					syncObject.wait();
					logger.info("Service check complete of device " + device.getFriendlyName(false) + ": " + responseCode);
					if(responseCode == 1)
						return true;
					return false;
				}
			} catch (BluetoothStateException e) {
				logger.warning("Bluetooth Stack exception occured: " + e.getMessage());
			} catch (InterruptedException e) {
				logger.warning("Thread was interrupted while serching for service: " + e.getMessage());
			} catch (IOException e) {
				logger.warning("IOException occured: " + e.getMessage());
			}
			return false;
		}
	}
	
	/**
	 * Checks range of the specified bluetooth device using handshake mechanism
	 * 
	 * @param mac MAC Address of the Bluetooth device whose range has to be checked
	 * @param service UUID in long format of the service to be considered
	 * 
	 * @return true: If found in the range else false
	 */
	public static boolean checkRange(String mac, long service) {
		RemoteDevice device = getRemoteDevice(mac);
		if(device == null)
			return false;
		else
			return checkRange(device, new UUID(service));
	}
	
	/**
	 * Returns RemoteDevice by MAC
	 * @param mac MAC address of the Device in String
	 * @return RemoteDevice
	 */
	public static RemoteDevice getRemoteDevice(String mac) {
		RemoteDevice[] devices = getPairedDevices();
		if(devices == null) 
			return null;
		for(RemoteDevice device: devices) {
			if(device.getBluetoothAddress().equals(mac))
				return device;
		}
		return null;
	}


}
