import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptOperator {
	static int bufferSize = 512*1024; //2Mb buffer
	
	/* Generates random bytes that is used as Initialization Vector for 
	 * encryption algorithm using SecureRandom class
	 * @return byte[] of generated IV
	 */
	public byte[] generateIv() {
		byte[] ivNum = new byte[16];
		SecureRandom random;
		try {
			random = SecureRandom.getInstance("AES");
			random.nextBytes(ivNum);
		} catch (NoSuchAlgorithmException e) {
			// TODO Add logging statements
			SOptions.showError("PROSecurity : Error-301", "An Error has occured while encrypting data. Error code:301");
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
	public void doOperation(int mode, SecretKey secretKey, String inputFile, String outputFile, byte[] ivNums) {
		try {
			//Buffers used in IO
			byte[] inBuffer = new byte[bufferSize];
			byte[] outBuffer = new byte[bufferSize];
			
			//Initializing Cipher
			IvParameterSpec ivSpec = new IvParameterSpec(ivNums);
			FileInputStream inputStream = new FileInputStream(inputFile);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
			cipher.init(mode, secretKey, ivSpec);
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

			// Read bytes from file and encrypt
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
		} catch (IOException e) {
			// TODO Add logging statements
			SOptions.showError("PROSecurity : Error-302", "An error has occured while trying to open files.");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Add logging statements
			SOptions.showError("PROSecurity : Error-303", "An error has occured.!");
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			SOptions.showError("PROSecurity : Error-304", "Decrypting file failed.!\nKey did not match.");
		} catch (InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
			// TODO
			SOptions.showError("PROSecurity : Error-305", "Decrypting file failed.!\nInvalid configuration");
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			SOptions.showError("PROSecurity : Error-306", "Decrypting file failed.!\nFile not correctly encrypted or File has been Corrupted/Tampered");
		}
	}

	
	/* Converts byte[] to SecretKey object used for storing IV in the KeyStore
	 * 
	 * @param byte[] containing the IV
	 * @return SecretKey object obtained from specified IV
	 */
	public SecretKey toSecretKey(byte[] ivNum) {
		return new SecretKeySpec(ivNum, 0, ivNum.length, "AES");
	}
	
	/* Converts SecretKey object to byte[] used for retrieving IV from KeyStore
	 * 
	 * @param SecretKey object to be converted
	 * @return byte[] converted from specified SecretKey
	 */
	public byte[] toByte(SecretKey key) {
		return key.getEncoded();
	}

}
