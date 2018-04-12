import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.eclipse.swt.widgets.Shell;

public class BTOperations {
	static boolean dontShow = false;
	static int responseCode;

	/*
	 * Checks if bluetooth device is on
	 * 
	 * @param useRecurssion if true Recursively checks whether is device on untill
	 * 'cancel' button is clicked
	 */
	public static boolean isPowerOn(boolean useRecurssion) {
		if (useRecurssion) {
			boolean retry = true;
			while (retry && !LocalDevice.isPowerOn()) {
				retry = SOptions.showConfirm(new Shell(), "Bluetooth Search failed - PROSecurity",
						"Failed to search for Bluetooth devices as Bluetooth is turned off. Please turn On the Bluetooth and Click 'OK' to retry.");
			}

			if (!LocalDevice.isPowerOn()) {
				dontShow = false;
				return false;
			}
			return LocalDevice.isPowerOn();
		} else {
			if (LocalDevice.isPowerOn()) {
				if (!dontShow) {
					SOptions.showInformation(new Shell(), "Bluetooth turnded off - PROSecurity",
							"As Bluetooth turned off, some functions off PROSecurity may not work"
									+ " untill its turned on.");
					dontShow = true;
				}
			}
			return LocalDevice.isPowerOn();
		}
	}

	/*
	 * Returns all Bluetooth devices paired with the current computer Check for
	 * isPowerOn() before calling this function
	 * 
	 * @return: RemoteDevice[] null if device is powered off
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

	/*
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
					if(responseCode == 1)
						return true;
					return false;
				}
			} catch (BluetoothStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
	}


}
