
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
import javax.crypto.SecretKey;

public class KeyStorage {
	/*
	 * Class KeyStorage provides interface for Storing and Retrieving Encryption keys from KeyStore
	 * 
	 * Constructor : 
	 * Pass fileKey in String format which is used for integrity checking
	 * Optional KeyStore file path, If not passed, Default KeyStore file will be used
	 */
	
	private FileInputStream fis = null;
	private File keyStoreFile = null;
	private String fileKeyString = null;

	// Constructor with fileKey only
	public KeyStorage(String fileKeyString) {
		this.fileKeyString = fileKeyString;
		this.keyStoreFile = new File("C:\\ProgramData\\PROSecurity\\Res\\DataStore.keystore");
	}

	//Constructor with File path and FileKey
	public KeyStorage(String fileKeyString, String keyStorePath) {
		this.fileKeyString = fileKeyString;
		this.keyStoreFile = new File(keyStorePath);
	}

	/* 
	 * Initializes and Returns the KeyStore Object
	 * If KeyStore file doesn't exits creates the new file
	 * 
	 * @throws: IOException if File Key is incorrect or File has been tampered
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
			// TODO Add logging statements
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Add logging statements
			System.out.println("IOException Occured.!");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* Retrieves the SecretKey with Specified entry name and entry password 
	 * @param entryName: Alias/Name of the entry
	 * @param secretKey: SecretKey object to be retrieved
	 * @param entryKeyString: Entry Protection password
	 * @return Secret Key object if entry is found and given password is correct
	 * @throws 	UnrecoverableEntryException: if given password is incorrect
	 * 			NullPointerException: if no entry is found with the given name
	 * 			IOException: if FileKey is incorrect or file has been tempered
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
			// TODO Add logging statements
			e.printStackTrace();
		} catch (UnrecoverableEntryException e) {
			// TODO Add logging statements
			System.out.println("UnrecoverableEntryException caught : " + e);
			throw e;
		} catch (KeyStoreException e) {
			// TODO Add logging statements
			e.printStackTrace();
		} finally {
			if(fis!=null) {
				try {
					// Close File
					fis.close();
				} catch (IOException e) {
					// TODO Add logging Statements
					e.printStackTrace();
				}
			}
		}
		if(entry==null) {
			throw new NullPointerException("Entry not found in the KeyStore");
		}
		return entry.getSecretKey();
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
