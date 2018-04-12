import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;

import org.eclipse.swt.widgets.Shell;

public class BTOperations {
	boolean dontShow = false;
	
	/*
	 * Checks if bluetooth device is on
	 * @param useRecurssion if true Recursively checks whether is device on untill 'cancel' button is clicked
	 */
	public static boolean isPowerOn(boolean useRecurssion) {
		if(useRecurssion) {
			boolean retry = true;
			while (retry && !LocalDevice.isPowerOn()) {
				retry = SOptions.showConfirm(new Shell(), "Bluetooth Search failed - PROSecurity",
						"Failed to search for Bluetooth devices as Bluetooth is turned off. Please turn On the Bluetooth and Click 'OK' to retry.");
			}

			if (!LocalDevice.isPowerOn()) {
				return false;
			}
			return LocalDevice.isPowerOn();
		} else {
			if(LocalDevice.isPowerOn()) {
				SOptions.showInformation(new Shell(), "Bluetooth turnded off - PROSecurity", "As Bluetooth turned off, some functions off PROSecurity may not work"
						+ " untill its turned on.");
			}
			return LocalDevice.isPowerOn();
		}
	}
	
	/*
	 * Returns all Bluetooth devices paired with the current computer
	 * Check for isPowerOn() before calling this function
	 * @return: RemoteDevice[]
	 * 			null if device is powered off
	 */
	public static RemoteDevice[] getPairedDevices() {
		try {
			return LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
		} catch (BluetoothStateException e) {
			//TODO
			e.printStackTrace();
			return null;
		}
	}
}
