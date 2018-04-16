import java.util.HashMap;

public class VolatileBag {
	// Threads
	public volatile static ServerThread serverThread;
	
	//KeyStorage
	public volatile static KeyStorage keyStorage;
	
	// Frame objects
	volatile static boolean isSafe = false;
	volatile static boolean isLock = false;
	volatile static boolean isOptions = false;
	volatile static boolean isAbout = false;
	volatile static boolean isExit = false;

	
	//HashMap
	public volatile static HashMap<String, Safe> safes = new HashMap<String, Safe>();
	
}
