import java.io.Serializable;

public class LockData implements Serializable {
	// Final Data
	private static final long serialVersionUID = 1L;
	
	// lock type values
	public static final int PRO_LOCK = 1;
	public static final int WIN_LOCK = 2;
	
	
	//Object Data
	private String mac;
	private int lockType;
	long service;
	
	/**
	 * Constructor: Initializes the object with MAC and lockType
	 * @param lockType int containing lock type WIN_LOCK or PRO_LOCK
	 * @param mac String containing MAC address of the device
	 */
	public LockData(int lockType, String mac, long service) {
		this.mac = mac;
		this.lockType = lockType;
		this.service = service;
	}
	
	/**
	 *@return lockType int 
	 */
	public int getLockType() {
		return lockType;
	}
	
	
	/**
	 * Returns mac address
	 * @return mac String
	 */
	public String getMac() {
		return mac;
	}
	
	/**
	 * Returns the service associated with the mac
	 * 
	 */
	public long getService() {
		return service;
	}

}
