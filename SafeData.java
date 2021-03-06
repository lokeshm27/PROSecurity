import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SafeData implements Serializable{
	// Final data
	private static final long serialVersionUID = 1L;
	public final static String loggerName = "default.runtime";
	public static final String safeExt = ".sdat";
	public static String rootPath = System.getenv("LocalAppData") + "\\PROSecurity";
	public static String resPath = rootPath + "\\Res";
	public static String tempPath = rootPath + "\\Temp";
	
	public static final int MAC_ONLY = 1;
	public static final int PWD_ONLY = 2;
	public static final int TWO_FACT = 3;
	
	// Object data
	protected String name;
	protected int lockType;
	protected String mac;
	protected long service;
	protected int size;
	protected String recoveryEmail;
	protected String hint = null;
	
	/**
	 * Initializes object for lock Type != PWD_ONLY
	 * 
	 * @param name String Safe name
	 * @param lockType Int
	 * @param mac String containing MAC address of the Bluetooth device
	 * @param service Long UUID of service to be used
	 * @param recoveryEmail E-Mail address used for device recovery
	 * @param hint String Used for password recovery
	 * 
	 * @throws IllegalArgumentException If lockType = PWD_ONLY
	 */
	public SafeData(String name, int lockType, String mac, long service, int size, String recoveryEmail, String hint) throws IllegalArgumentException {
		if(lockType == PWD_ONLY) {
			throw new IllegalArgumentException("Illegal Argument: lockType");
		}
		this.name = name;
		this.lockType = lockType;
		this.mac = mac;
		this.service = service;
		this.size = size;
		this.recoveryEmail = recoveryEmail;
		this.hint = hint;
	}
	
	
	/**
	 * Initializes object with safeName and lockType = PWD_ONLY.
	 * 		Use other constructor for other case
	 * 
	 * @param name String Safe Name
	 * @param size Int Size of the safe
	 * @param hint String Used for password recovery
	 */
	public SafeData(String name, int size, String hint) {
		this.name = name;
		this.lockType = PWD_ONLY;
		this.size = size;
		this.hint = hint;
	}
	
	/**
	 * Copy constructor
	 * @param safeData SafeData object from which data has to be copied
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
				this.mac = safeData.getMac();
				this.service = safeData.getService();
				this.recoveryEmail = safeData.getRecoveryEmail();
			} catch (IllegalAccessException e) {
				// DO Nothing
			}
		}
	}

	/**
	 * @return Name of the safe
	 */
	public String getName() {
		return name;
	}
	 
	
	/**
	 *@return lockType 
	 */
	public int getLockType() {
		return lockType;
	}
	
	
	/**
	 * @return MAC address
	 * @throws IllegalAccessException If lockType is set to PWD_ONLY
	 */
	public String getMac() throws IllegalAccessException {
		if(lockType != PWD_ONLY) {
			return mac;
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}

	/**
	 * @return UUID of service to be used
	 * @throws IllegalAccessException If lockType is set to PWD_ONLY
	 */
	public long getService() throws IllegalAccessException {
		if(lockType != PWD_ONLY) {
			return this.service;
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}
	
	/**
	 * Sets the service of the bluetooth device
	 * @param service Long UUID of the service
	 */
	public void setService(long service) {
		if(lockType != PWD_ONLY) {
			this.service = service;
		}
	}
	
	/**
	 * @return size of the safe
	 */
	public int getSize() {
		return size;
	}

	
	/** 
	 * @return recovery Email
	 * @throws IllegalAccessException: If lockType is set to PWD_ONLY
	 */
	public String getRecoveryEmail() throws IllegalAccessException  {
		if(lockType != PWD_ONLY) {
			return recoveryEmail; 
		} else {
			throw new IllegalAccessException("Illegal Access to MAC : Not Defined");
		}
	}


	/**
	 * @return true: if hint is set.
	 * 		   false: if hint is not set i.e. hint == null
	 */
	public boolean isSetHint() {
		if(hint == null) {
			return false;
		}
		return true;
	}

	
	/**
	 * @return Hint
	 * 
	 * @throws IllegalAccessException If hint == null
	 */
	public String getHint() throws IllegalAccessException {
		if(hint == null) {
			throw new IllegalAccessException("IllegalAccess to hint: Not defined");
		}
		return hint;
	}


	/** returns safe file name
	 * @return String: Safe file name
	 */
	public String getSafeFileName() {
		return name + "$"+ recoveryEmail;
	}

	/**
	 * Writes the attributes of this object to SafeName.sdat file in Res folder using serialization
	 * @throws IOException If failed to serialize contents
	 */
	public void serial() throws IOException {
		File safeDat = new File(resPath + "\\" + this.name + safeExt);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(safeDat));
		oos.writeObject(this);
		oos.flush();
		oos.close();
	}

}
