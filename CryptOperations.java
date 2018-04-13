import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.swt.widgets.Shell;

public class CryptOperations {
	public final String loggerName = "default.runtime";
	
	static int bufferSize = 512*1024; //2Mb buffer
	static Logger logger;
	
	/*
	 * Constructor 
	 * Initializes logger
	 */
	public void init() {
		logger = Logger.getLogger(loggerName);
		logger.info("CryptOperator Initialized");
	}
	
	
	/* Generates random bytes that is used as Initialization Vector for 
	 * encryption algorithm using SecureRandom class
	 * @return byte[] of generated IV
	 */
	public static byte[] generateIv() {
		byte[] ivNum = new byte[16];
		SecureRandom random;
		try {
			logger.info("Generating Initialization Vector IV");
			random = SecureRandom.getInstance("AES");
			random.nextBytes(ivNum);
		} catch (NoSuchAlgorithmException e) {
			logger.severe("NoSuchAlgorithmException caught while generating IV");
			SOptions.showError(new Shell(), "PROSecurity : Error-301", "An Error has occured while encrypting data. Error code:301");
		}
		return ivNum;
	}
	
	
	/* Encrypts or Decrypts File using AES-128 algorithm
	 * 
	 * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @param secretKey encryption key to be used
	 * @param inputFile String containing path of input file
	 * @param outputFile String containing path of output file
	 */
	public static void doOperation(int mode, SecretKey secretKey, String inputFile, String outputFile, byte[] ivNums) {
		try {
			//Buffers used in IO
			byte[] inBuffer = new byte[bufferSize];
			byte[] outBuffer = new byte[bufferSize];
			
			//Initializing Cipher
			logger.info("Initializing cipher\nMode: " + mode + " inputFile: " + inputFile);
			IvParameterSpec ivSpec = new IvParameterSpec(ivNums);
			FileInputStream inputStream = new FileInputStream(inputFile);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
			cipher.init(mode, secretKey, ivSpec);
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

			// Read bytes from file and encrypt
			logger.info("reading file..");
			int count = 0;
			while((count = inputStream.read(inBuffer)) != -1) {
				outBuffer = cipher.update(inBuffer, 0, count);
				
				// write to output file
				if(outBuffer != null)
					cipherOutputStream.write(outBuffer);
			}
			outBuffer = cipher.doFinal();
			if(outBuffer != null)
				cipherOutputStream.write(outBuffer);
			
			// Flush and Close files
			cipherOutputStream.flush();
			cipherOutputStream.close();
			inputStream.close();
			logger.fine("Operation complete");
		} catch (IOException e) {
			logger.severe("IOException caught file reading/writing file " + e.getMessage());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.severe("NoSuchAlgorithm exception caught: " + e.getMessage());
		} catch (InvalidKeyException e) {
			logger.severe("InvalidKeyException caught: " + e.getMessage());
			SOptions.showError(new Shell(), "PROSecurity : Error-304", "Crypt operation on files failed. May be due to mismatch in the key");
		} catch (InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
			logger.severe("InvalidAlgorithmParamentException caught: " + e.getMessage());
		} catch (BadPaddingException e) {
			logger.severe("BadPaddingException caught: " + e.getMessage() + " File not correctle encrypted or file has been tampered");
			SOptions.showError(new Shell(), "PROSecurity : Error-306", "Decrypting file failed.!\nFile not correctly encrypted or File has been Corrupted/Tampered");
		}
	}

	
	/* Converts byte[] to SecretKey object used for storing IV in the KeyStore
	 * 
	 * @param byte[] containing the IV
	 * @return SecretKey object obtained from specified IV
	 */
	public static SecretKey toSecretKey(byte[] ivNum) {
		return new SecretKeySpec(ivNum, 0, ivNum.length, "AES");
	}
	
	/* Converts SecretKey object to byte[] used for retrieving IV from KeyStore
	 * 
	 * @param SecretKey object to be converted
	 * @return byte[] converted from specified SecretKey
	 */
	public static byte[] toByte(SecretKey key) {
		return key.getEncoded();
	}

}
