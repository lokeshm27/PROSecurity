import java.util.logging.Logger;

public class SearchThread extends Thread {
	private boolean cancel = false, refresh = false;
	public final String loggerName = "default.runtime";
	Logger logger;

	public SearchThread() {
		logger = Logger.getLogger(loggerName);
		logger.info("Sreach thread initiated");
	}

	@Override
	public void run() {
		while (!cancel) {
			try {
				for (Safe safe : VolatileBag.safes.values()) {
					try {
						if (!safe.isUnlocked()) {
							logger.info("SearchThread checking Safe: " + safe.getName());
							if (!safe.isAuthorized()) {
								// Safes which are not authorized
								if (safe.getLockType() == Safe.PWD_ONLY) {
									logger.info("Safe " + safe.getName() + " authorized: PWD_ONLY" );
									safe.setAuthorized(true);
								} else {
									if (BTOperations.checkRange(safe.getMac(), safe.getService())) {
										logger.info("Safe " + safe.getName() + " authorized: Device " + safe.getMac() + " found." );
										safe.setAuthorized(true);
									}
								}
							} else {
								// Safes which are authorized
								if (safe.getLockType() != Safe.PWD_ONLY) {
									if (!BTOperations.checkRange(safe.getMac(), safe.getService())) {
										logger.info("Safe " + safe.getName() + " unauthorized: Device " + safe.getMac() + " not found." );
										safe.setAuthorized(false);
									}
								}

							}
						}
					} catch (IllegalAccessException e) {
						logger.warning("IllegalAccessException caught: " + e.getMessage());
					}
					Thread.sleep(getSleepTime());
				}
			} catch (InterruptedException e) {
				logger.warning("SearchThread interrupted: " + e.getMessage());
			}
			refresh = false;
		}
	}

	public void refresh() {
		if(!refresh) {
			refresh = true;
			Thread.currentThread().interrupt();
		}
	}

	long getSleepTime() {
		int size = VolatileBag.safes.size();
		if(refresh) {
			return 0;
		} else if (size <= 1) {
			return 2000;
		} else if (size <= 2) {
			return 1000;
		} else if (size <= 5) {
			return 500;
		} else
			return 200;
	}

	public void cancel() {
		cancel = true;
		Thread.currentThread().interrupt();
	}
}
