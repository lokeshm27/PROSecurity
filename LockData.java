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
	
	/*
	 * Constructor: Initializes the object with MAC and lockType
	 * @param mac: String containing MAC address of the device
	 */
	public LockData(String mac) throws IllegalArgumentException {
		this.mac = mac;
		this.lockType = PRO_LOCK;
	}
	
	
	/*
	 * Constructor: Initializes object with lockType = WIN_LOCK
	 * 
	 * Use other constructor for other case
	 */
	public LockData() {
		this.lockType = WIN_LOCK;
	}
	
	
	/*
	 *@return lockType: int 
	 */
	public int getLockType() {
		return lockType;
	}
	
	
	/*
	 * @return mac: String
	 * @throws IllegalAccessException: If lockType is set to WIN_LOCK
	 */
	public String getMac() throws IllegalAccessException {
		if(lockType == PRO_LOCK) {
			return mac;
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}
	

}
