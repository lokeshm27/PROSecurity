import java.io.Serializable;

public class SafeData implements Serializable{
	// Final data
	private static final long serialVersionUID = 1L;
	
	public static final int MAC_ONLY = 1;
	public static final int PWD_ONLY = 2;
	public static final int TWO_FACT = 3;
	
	// Object data
	public String safeName;
	public int lockType;
	public String mac;
	
	
	/*
	 * Constructor: Initializes object with safeName, lockType and MAC address
	 * 
	 * @throws IllegalArgumentException: if lockType = PWD_ONLY
	 */
	public SafeData(String safeName, int lockType, String mac) throws IllegalArgumentException {
		if(lockType == PWD_ONLY) {
			throw new IllegalArgumentException("Illegal Argument: lockType");
		}
		this.safeName = safeName;
		this.lockType = lockType;
		this.mac = mac;
	}
	
	
	/*
	 * Constructor: Initializes object with safeName and lockType = PWD_ONLY
	 * 
	 * 	Use other constructor for other case
	 */
	public SafeData(String safeName) {
		this.safeName = safeName;
		this.lockType = PWD_ONLY;
	}
	
	
	/*
	 * @return safeName: String
	 */
	public String getSafeName() {
		return safeName;
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
