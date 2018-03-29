import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class StartingPoint {
	
	public static String rootPath = System.getenv("LocalAppData") + "\\PROSecurity";
	public static String resPath = rootPath + "\\Res";
	public static String tempPath = rootPath + "\\Temp";
	
	public static void mian(String args[]) {
		FileLock lock;
		FileChannel channel;
		
		// Check if One-time initializtion is required
		if(!new File(rootPath).exists()) {
			init();
			serverMode();
		} else {
			File lockFile = new File(tempPath + "\\lock.tmp");
			try {
				// Acquire lock
				channel = new RandomAccessFile(lockFile, "rw").getChannel();
				lock = channel.tryLock();
				
				if(lock == null) {
					// Instance already running
					clientMode();
				} else {
					
					// Add a shutdown hook to release lock
					Runtime.getRuntime().addShutdownHook(new Thread() {
						public void run() {
							try {
								lock.release();
								channel.close();
							} catch (Exception e ) {
								e.printStackTrace();
							}
						}
					});
					serverMode();
				}
			} catch (FileNotFoundException e) {
				// TODO
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void init() {
		
		File rootFolder = new File(rootPath);
		File resFolder = new File(resPath);
		File tempFolder = new File(tempPath);
		
		if(rootFolder.mkdirs() || resFolder.mkdirs() || tempFolder.mkdirs()) {
			SOptions.showError("PROSecurity - Initialization Error", "PROSecurity couldn't craete necessary folders in the path "+ System.getenv("LocalAppData")
					+ " for operation. PROSecurity may require additional Previlages.");
			System.exit(-1);
		}
	}
	
	public static void serverMode() {
		
	}
	
	public static void clientMode() {
		
	}
}
