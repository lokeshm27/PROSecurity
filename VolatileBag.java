import java.awt.TrayIcon;
import java.util.HashMap;

public class VolatileBag {
	// Status and TrayIcon
	volatile static boolean status;
	volatile static TrayIcon tryaIcon;
	
	// Threads
	volatile static ServerThread serverThread;
	
	// Frame boolean
	volatile static boolean isSafe = false;
	volatile static boolean isLock = false;
	volatile static boolean isOptions = false;
	volatile static boolean isAbout = false;
	volatile static boolean isExit = false;
	
	// Frame objects
	
	//HashMap
	volatile static HashMap<String, Safe> safes = new HashMap<String, Safe>();
	
	
}
