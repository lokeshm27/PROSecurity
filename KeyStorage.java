
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

public class KeyStorage {
	/*
	 * Class KeyStorage provides interface for Storing and Retrieving Encryption keys from KeyStore
	 * 
	 * Constructor : 
	 * Pass fileKey in String format which is used for integrity checking
	 * Optional KeyStore file path, If not passed, Default KeyStore file will be used
	 */
	
	public final static String loggerName = "default.runtime";

	static Logger logger;
	private FileInputStream fis = null;
	private File keyStoreFile = null;
	private String fileKeyString = null;

	// Constructor with fileKey only
	public KeyStorage(String fileKeyString) {
		this.fileKeyString = fileKeyString;
		this.keyStoreFile = new File(System.getenv("LocalAppData") + "\\PROSecurity\\Res\\DataStore.keystore");
		logger = Logger.getLogger(loggerName);
		logger.info("KeyStorage initialized with FileKey Only");
	}

	//Constructor with File path and FileKey
	public KeyStorage(String fileKeyString, String keyStorePath) {
		this.fileKeyString = fileKeyString;
		this.keyStoreFile = new File(keyStorePath);
		logger = Logger.getLogger(loggerName);
		logger.info("KeyStorage initialized with FileKey and keyStorePath");
	}

	/* 
	 * Initializes and Returns the KeyStore Object
	 * If KeyStore file doesn't exits creates the new file
	 * 
	 * @throws IOException: if File Key is incorrect or File has been tampered
	 */
	private KeyStore getKeyStore() throws IOException{
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance("JCEKS");
			if (keyStoreFile.exists()) {
				fis = new FileInputStream(keyStoreFile);
				ks.load(fis, fileKeyString.toCharArray());
			} else {
				ks.load(null, null);
				ks.store(new FileOutputStream(keyStoreFile), fileKeyString.toCharArray());
			}
		} catch (KeyStoreException e) {
			logger.severe("KeyStorage Exception caugt: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.severe("NoSuchAlgorithException caught: " + e.getMessage());
		} catch (CertificateException e) {
			logger.severe("CertificateException caught: " + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.severe("FileNotFoundException caught: " + e.getMessage());
		} catch (IOException e) {
			logger.warning("IOException caught: " + e.getMessage());
			throw e;
		}
		return ks;
	}

	/* Store the SecretKey in the KeyStore file
	 * @param entryName: Alias/Name of the entry
	 * @param secretKey: SecretKey object to be stored
	 * @param entryKeyString: Entry Protection password
	 * @throws IOException: if FileKey is incorrect or file has been tempered
	 */
	public void storeKey(String entryName, SecretKey secretKey, String entryKeyString) throws IOException{
		// Obtain KeyStore
		KeyStore keyStore = getKeyStore();
		
		// Create entry level protection using entry key
		ProtectionParameter param = new PasswordProtection(entryKeyString.toCharArray());
		SecretKeyEntry entry = new SecretKeyEntry(secretKey);

		FileOutputStream fos = null;
		try {
			// Add entry and Store keys to the file
			fos = new FileOutputStream(keyStoreFile);
			keyStore.setEntry(entryName, entry, param);
			keyStore.store(fos, fileKeyString.toCharArray());
		} catch (KeyStoreException e) {
			logger.severe("KeyStorage Exception caugt: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.severe("NoSuchAlgorithException caught: " + e.getMessage());
		} catch (CertificateException e) {
			logger.severe("CertificateException caught: " + e.getMessage());
		} catch (IOException e) {
			logger.severe("IOException caught: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
	}

	/* Retrieves the SecretKey with Specified entry name and entry password 
	 * @param entryName: Alias/Name of the entry
	 * @param entryKeyString: Entry Protection password
	 * @return Secret Key object if entry is found and given password is correct
	 * @throws UnrecoverableEntryException: if given password is incorrect
	 * @throws NullPointerException: if no entry is found with the given name
	 * @throws IOException: if FileKey is incorrect or file has been tempered
	 */
	public SecretKey getKey(String entryName, String entryKeyString) throws UnrecoverableEntryException, NullPointerException, IOException {
		// Get KeyStore
		KeyStore keyStore = getKeyStore();
		
		ProtectionParameter param = new PasswordProtection(entryKeyString.toCharArray());
		
		// Get Entry from KeyStore
		SecretKeyEntry entry = null;
		try {
			entry = (SecretKeyEntry) keyStore.getEntry(entryName, param);
		} catch (NoSuchAlgorithmException e) {
			logger.severe("NoSuchAlgorithmException caught: " + e.getMessage());
		} catch (UnrecoverableEntryException e) {
			logger.warning("UnrecoverableEntryException caught : " + e);
			throw e;
		} catch (KeyStoreException e) {
			logger.severe("KeyStoreExceptionCaught: " + e.getMessage());
		} finally {
			if(fis!=null) {
				try {
					// Close File
					fis.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
		if(entry==null) {
			throw new NullPointerException("Entry not found in the KeyStore");
		}
		return entry.getSecretKey();
	}

	/* Deletes the Entry with the specified Name 
	 * @param entryName: Alias/Name of the entry
	 * @throws IOException: if given password is incorrect
	 * @throws IllegalArgumentException: if given key String is incorrect
	 * @throws NullPointerException: if entry with the given name doesn't exists
	 */
	public void deleteEntry(String entryName, String entryKeyString) throws IOException, IllegalArgumentException {
		KeyStore keyStore = getKeyStore();
		try {
			if(!keyStore.containsAlias(entryName)) {
				throw new NullPointerException("Entry not found " + entryName);
			}
			
			try {
				getKey(entryName, entryKeyString);
			} catch (UnrecoverableEntryException e) {
				throw new IllegalArgumentException("Invalid key for entry: " + entryName);
			} catch(NullPointerException e) {
				// Do nothing, Will never encounter
			}
			keyStore.deleteEntry(entryName);
		} catch (KeyStoreException e) {
			logger.severe("KeyStoreException caught: " + e.getMessage());
		}
	}
	
	/* Returns the Secret Key in string format
	 * Obtains the SecretKey object using getKey() function and Converts it to String
	 * 
	 * NOTE: Do not use this function in actual code
	 * 		 Use only for debugging operations
	 */	
	public String getKeyString(String entryName, String entryKeyString) throws UnrecoverableEntryException, NullPointerException, IOException {
		SecretKey secretKey = getKey(entryName, entryKeyString);
		return Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}
}
