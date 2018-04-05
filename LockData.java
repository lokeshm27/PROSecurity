import java.io.Serializable;

public class LockData implements Serializable {
	// Final Data
	private static final long serialVersionUID = 1L;
	
	public static final int MAC_ONLY = 1;
	public static final int PWD_ONLY = 2;
	public static final int TWO_FACT = 3;
	
	//Object Data
	private String mac;
	private int lockType;
	
	/*
	 * Constructor: Initializes the object with MAC and lockType
	 * @param mac: String containing MAC address of the device
	 * @param lockType: int containing the lockType
	 * @throws IllegalArgumentException: If lockType = PWD_ONLY
	 */
	public LockData(String mac, int lockType) throws IllegalArgumentException {
		if(lockType == PWD_ONLY) {
			throw new IllegalArgumentException("Invalid arguments: lockType");
		}
		this.mac = mac;
		this.lockType = lockType;
	}
	
	
	/*
	 * Constructor: Initializes object with lockType = PWD_ONLY
	 * 
	 * Use other constructor for other cases
	 */
	public LockData() {
		this.lockType = PWD_ONLY;
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
			return mac;
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}


}
