import java.awt.TrayIcon;
import java.util.logging.Logger;

public class SmartLockThread extends Thread {
	private LockData data;
	public final String loggerName = "default.runtime";
	Logger logger;
	boolean cancel = false;
	boolean activated = false;
	
	public SmartLockThread(LockData data) {
		this.data = data;
		logger = Logger.getLogger(loggerName);
		logger.info("SmartLockThread initialized");
	}
	
	public void run() {
		cancel = false;
		if(!BTOperations.checkRange(data.getMac(), data.getService())) {
			logger.info("Smart Lock inactive");
			TrayOperations.toYellow("Smart Lock not active");
			TrayOperations.displayMessage("PROSecurity - Smart Lock not active", "Smart Lock not activated as the configured bluetooth device not found", 
					TrayIcon.MessageType.WARNING);
			while (!cancel && !Thread.currentThread().isInterrupted()) {
				System.out.println("Inactive Check : " + data.getMac() + " Service: " + data.getService());
				if(!BTOperations.checkRange(data.getMac(), data.getService())) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.warning("SmartLockThread interrrupted while inactive sleep");
					}
				} else {
					
					TrayOperations.toGreen("Smart Lock not active");
					TrayOperations.displayMessage("PROSecurity - Smart Lock active", "Smart Lock activated as the configured bluetooth device found", 
							TrayIcon.MessageType.INFO);
					break;
				}
			}
			if(cancel) {
				return;
			}
		} else {
			logger.info("Activating SmartLock");
			TrayOperations.toGreen("Smart Lock not active");
			TrayOperations.displayMessage("PROSecurity - Started", "PROSecurity has started operating in normal mode.",
					TrayIcon.MessageType.INFO);
		}
		
		int j=0;
		while (!cancel && !Thread.currentThread().isInterrupted()) {
			System.out.println("Active Check");
			if(BTOperations.checkRange(data.getMac(), data.getService())) {
				j=0;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.warning("SmartLockThread interrrupted while active sleep");
				}
			} else {
				if(j==3)
					break;
				j++;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					logger.warning("SmartLockThread interrrupted while confirm sleep");
				}
				
			}
		}
		if(j==3) {
			// Locking computer
			if(data.getLockType() == LockData.WIN_LOCK)
				winLock();
			else
				proLock();
		}
		
	}
	
	public void cancel() {
		cancel = true;
		Thread.currentThread().interrupt();
	}
	
	private void winLock() {
		System.out.println("Windows Lock");
	}
	
	private void proLock() {
		System.out.println("PROLock");
	}
}
