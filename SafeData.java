import java.io.IOException;
import java.io.Serializable;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;

public class SafeData implements Serializable{
	// Final data
	private static final long serialVersionUID = 1L;
	
	public static final int MAC_ONLY = 1;
	public static final int PWD_ONLY = 2;
	public static final int TWO_FACT = 3;
	
	// Object data
	protected String name;
	protected int lockType;
	protected RemoteDevice device;
	protected UUID service;
	protected int size;
	protected String recoveryEmail;
	protected String hint = null;
	
	/*
	 * Constructor: Initializes object for lock Type != PWD_ONLY
	 * 
	 * @param name: String Safe name
	 * @param lockType: Int
	 * @param device: Bluetooth device
	 * @param service: UUID of service to be used
	 * @param recoveryEmail: E-Mail address used for device recovery
	 * @param hint: String Used for password recovery
	 * 
	 * @throws IllegalArgumentException: if lockType = PWD_ONLY
	 */
	public SafeData(String name, int lockType, RemoteDevice device, UUID service, int size, String recoveryEmail, String hint) throws IllegalArgumentException {
		if(lockType == PWD_ONLY) {
			throw new IllegalArgumentException("Illegal Argument: lockType");
		}
		this.name = name;
		this.lockType = lockType;
		this.device = device;
		this.service = service;
		this.size = size;
		this.recoveryEmail = recoveryEmail;
		this.hint = hint;
	}
	
	
	/*
	 * Constructor: Initializes object with safeName and lockType = PWD_ONLY
	 * 		Use other constructor for other case
	 * 
	 * @param name: String Safe Name
	 * @param hint: String Used for password recovery
	 */
	public SafeData(String name, int size, String hint) {
		this.name = name;
		this.lockType = PWD_ONLY;
		this.size = size;
		this.hint = hint;
	}
	
	/*
	 * Copy constructor
	 * @param: SafeData object
	 */
	public SafeData(SafeData safeData) {
		this.name = safeData.getName();
		this.size = safeData.getSize();
		if(safeData.getLockType() == PWD_ONLY) {
			this.lockType = PWD_ONLY;
			try {
				this.hint = safeData.getHint();
			} catch (IllegalAccessException e) {
				this.hint = null;
			}
		} else {
			try {
				this.lockType = safeData.getLockType();
				this.device = safeData.getDevice();
				this.service = safeData.getService();
				this.recoveryEmail = safeData.getRecoveryEmail();
			} catch (IllegalAccessException e) {
				// DO Nothing
			}
		}
	}

	/*
	 * @return safeName: String
	 */
	public String getName() {
		return name;
	}
	 
	
	/*
	 *@return lockType: int 
	 */
	public int getLockType() {
		return lockType;
	}
	
	
	/*
	 * @return mac: String
	 * @throws IllegalAccessException: If lockType is set to PWD_ONLY
	 */
	public String getMac() throws IllegalAccessException {
		if(lockType != PWD_ONLY) {
			return device.getBluetoothAddress();
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}
	
	/*
	 * @return Device's friendly name
	 * @throws IllegalAccessException: If lockType is set to PWD_ONLY
	 */
	public String getDeviceName() throws IllegalAccessException {
		if(lockType != PWD_ONLY) {
			try {
				return device.getFriendlyName(false);
			} catch (IOException e) {
				throw new IllegalAccessException("IOExceptio caught while retrieving name: " + e);
			}
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}
	
	/*
	 * @return RemoteDevice
	 * @throws IllegalAccessException: If lockType is set to PWD_ONLY
	 */
	public RemoteDevice getDevice() throws IllegalAccessException {
		if(lockType != PWD_ONLY) {
			return this.device;
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}

	/*
	 * @return UUID of service to be used
	 * @throws IllegalAccessException: If lockType is set to PWD_ONLY
	 */
	private UUID getService() throws IllegalAccessException {
		if(lockType != PWD_ONLY) {
			return this.service;
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}
	
	/*
	 * @return size: Int
	 */
	public int getSize() {
		return size;
	}

	
	/*
	 * @return recoveryEmail: String
	 * 
	 * @throws IllegalAccessException: If lockType is set to PWD_ONLY
	 */
	public String getRecoveryEmail() throws IllegalAccessException  {
		if(lockType != PWD_ONLY) {
			return recoveryEmail; 
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}


	/*
	 * @return true: if hint is set
	 * 		   false: if hint is not set i.e. hint == null
	 */
	public boolean isSetHint() {
		if(hint == null) {
			return false;
		}
		return true;
	}

	
	/*
	 * @return hint: String
	 * 
	 * @throws IllegalAccessException: If hint == null
	 */
	public String getHint() throws IllegalAccessException {
		if(hint == null) {
			throw new IllegalAccessException("IllegalAccess to hint: Not defined");
		}
		return hint;
	}


	/* returns safe file name
	 * @return String: Safe file name
	 */
	public String getSafeFileName() {
		return name + "$"+ recoveryEmail;
	}


}
