import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.crypto.SecretKey;

public class Safe extends SafeData {
	private static final long serialVersionUID = 1L;

	private boolean unlocked = false;
	private SecretKey secretKey;
	
	
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
	public Safe(String name, int lockType, RemoteDevice device, UUID service, int size, String recoveryEmail, String hint) throws IllegalArgumentException {
		super(name, lockType, device, service, size, recoveryEmail, hint);
	}
	

	/*
	 * Constructor: Initializes object with safeName and lockType = PWD_ONLY
	 * 		Use other constructor for other case
	 * 
	 * @param name: String Safe Name
	 * @param recoveryEmail: E-Mail address used for device recovery
	 * @param hint: String Used for password recovery
	 */
	public Safe(String name, int size, String hint) {
		super(name, size, hint);
		// TODO
	}
	
	/*
	 * Constructor to Initialize Safe object from SafeData object
	 * @param SafeData object
	 */
	public Safe(SafeData safeData) {
		super(safeData);
	}
	
	/*
	 * @return SafeData: Parent class object
	 */
	public SafeData getSafeData() {
		return this;
	}

	
	/*
	 * sets unlocked to true
	 */
	public void setUnlocked() {
		unlocked = true;
	}
	
	
	/*
	 * sets unlocked to false
	 */
	public void setLocked() {
		unlocked = false;
	}
	
	
	/*
	 * @return true: If unlocked = true
	 * 		   false: Otherwise
	 */
	public boolean isUnlocked() {
		return unlocked;
	}

	/*
	 * Sets SecretKey Object
	 * 
	 * @param SecretKey
	 * 
	 * @throws IllegalArgumentException: If unlocked = true
	 */
	public void setSecretKey(SecretKey secretKey) throws IllegalArgumentException {
		if(unlocked) {
			throw new IllegalArgumentException("IllegalArgument SecretKey: Unlocked = true");
		}
		this.secretKey = secretKey;
	}
	
	
	/*
	 * Gets SecretKey Object
	 * 
	 * @return SecretKey
	 * 
	 * @throws IllegalAccessException: If unlocked = false
	 */
	public SecretKey getSecretKey() throws IllegalAccessException {
		if(!unlocked) {
			throw new IllegalAccessException("Illegal Access to SecretKey: Unlocked = false");
		}
		return secretKey;
	}


}
