import java.util.concurrent.ConcurrentHashMap;

public class VolatileBag {
	// Threads
	public volatile static ServerThread serverThread;
	public volatile static SearchThread searchThread;
	public volatile static SmartLockThread smartLockThread;
	
	//KeyStorage
	public volatile static KeyStorage keyStorage;
	
	// Frame objects
	volatile static boolean isSafe = false;
	volatile static boolean isLock = false;
	volatile static boolean isOptions = false;
	volatile static boolean isAbout = false;
	volatile static boolean isExit = false;
	volatile public static boolean safeOngoing = false;
	volatile static SafeActionListener safeListener;

	
	//HashMap
	public volatile static ConcurrentHashMap<String, Safe> safes = new ConcurrentHashMap<String, Safe>();
	
	public static void updateSafe() {
		try {
			if(!safeOngoing) {
			safeListener.shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					safeListener.buildList();
				}
			});
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
