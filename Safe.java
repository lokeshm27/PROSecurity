import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.UnrecoverableEntryException;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Safe extends SafeData {

	private static final long serialVersionUID = 1L;
	private boolean unlocked = false;
	private boolean authorized = false;
	private SecretKey secretKey;
	private byte[] ivNums;
	private Logger logger;
	private String password = null;
	private Thread watchThread;
	private boolean cancel = false;
	private boolean watchRunning = false;

	/**
	 * Initializes object for lock Type != PWD_ONLY
	 * 
	 * @param name
	 *            String Safe name
	 * @param lockType
	 *            Int
	 * @param mac
	 *            String containing MAC address Bluetooth device
	 * @param service
	 *            long UUID of service to be used
	 * @param recoveryEmail
	 *            String E-Mail address used for device recovery
	 * @param hint
	 *            String Used for password recovery
	 * 
	 * @throws IllegalArgumentException
	 *             If lockType = PWD_ONLY
	 */
	public Safe(String name, int lockType, String mac, long service, int size, String recoveryEmail, String hint)
			throws IllegalArgumentException {
		super(name, lockType, mac, service, size, recoveryEmail, hint);
		logger = Logger.getLogger(loggerName);
	}

	/**
	 * Initializes object with safeName and lockType = PWD_ONLY. Use other
	 * constructor for other case
	 * 
	 * @param name
	 *            String Safe Name
	 * @param recoveryEmail
	 *            E-Mail address used for device recovery
	 * @param hint
	 *            String Used for password recovery
	 */
	public Safe(String name, int size, String hint) {
		super(name, size, hint);
		logger = Logger.getLogger(loggerName);
		authorized = true;
	}

	/**
	 * Constructor to Initialize Safe object from SafeData object
	 * 
	 * @param safeData
	 *            SafeData object
	 */
	public Safe(SafeData safeData) {
		super(safeData);
		logger = Logger.getLogger(loggerName);
		if (lockType == PWD_ONLY)
			authorized = true;
	}

	/**
	 * Returns SafeData object of this Safe
	 * 
	 * @return SafeData Parent class object
	 */
	public SafeData getSafeData() {
		return this;
	}

	/**
	 * sets unlocked to true
	 */
	public void setUnlocked() {
		unlocked = true;
	}

	/**
	 * sets unlocked to false
	 */
	public void setLocked() {
		unlocked = false;
	}

	/**
	 * Return whether this safe is locked or unlocked
	 * 
	 * @return true: If unlocked = true false: Otherwise
	 */
	public boolean isUnlocked() {
		return unlocked;
	}

	/**
	 * Sets the authorized value to the given value
	 * 
	 * @param authorized
	 *            Boolean value to be set
	 */
	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	/**
	 * Returns the authorized value of the safe
	 * 
	 * @return boolean: authorized value
	 */
	public boolean isAuthorized() {
		return authorized;
	}

	/**
	 * Sets SecretKey Object
	 * 
	 * @param secretKey
	 *            SecretKey object associated with this safe
	 * 
	 * @throws IllegalArgumentException:
	 *             If unlocked = true
	 */
	public void setSecretKey(SecretKey secretKey) throws IllegalArgumentException {
		if (unlocked) {
			throw new IllegalArgumentException("IllegalArgument SecretKey: Unlocked = true");
		}
		this.secretKey = secretKey;
	}

	/**
	 * Gets SecretKey Object
	 * 
	 * @return SecretKey Object associated with this safe
	 * 
	 * @throws IllegalAccessException:
	 *             If unlocked = false
	 */
	/*
	 * public SecretKey getSecretKey() throws IllegalAccessException { if
	 * (!unlocked) { throw new
	 * IllegalAccessException("Illegal Access to SecretKey: Unlocked = false"); }
	 * return secretKey; }
	 */

	/**
	 * Sets the ivNums for this safe
	 * 
	 * @param ivNums
	 *            byte[] containing ivNums
	 * @throws IllegalArgumentException
	 *             if unlocked == true
	 */
	public void setIvNums(byte[] ivNums) throws IllegalArgumentException {
		if (unlocked) {
			throw new IllegalArgumentException("IllegalArgument SecretKey: Unlocked = true");
		}
		this.ivNums = ivNums;
	}

	/**
	 * Unlocks the safe and attaches the disk to the device
	 */
	public void unlock() {
		VolatileBag.safeOngoing = true;
		boolean wrongPassword = false;
		// Check for authorization
		System.out.println("Trying to unlock safe: " + name + " lockType: " + lockType);
		logger.info("Trying to unlock safe: " + name + " lockType: " + lockType);
		if (lockType == MAC_ONLY || lockType == TWO_FACT) {
			logger.info("Checking for bluetooth device: " + mac);
			if (!BTOperations.checkRange(mac, service)) {
				logger.warning("BT Device not found. Returning...");
				authorized = false;
				SOptions.showError(null, "Error - PROSecurity",
						"Unlocking safe " + name + " failed. Bluetooth device not found.!");
				VolatileBag.safeOngoing = false;
				return;
			}
			logger.fine("Bluetooth device found.!");
		}

		if (lockType == MAC_ONLY) {
			try {
				secretKey = VolatileBag.keyStorage.getKey(name, KeyStorage.defaultPassword);
				ivNums = CryptOperations
						.toByte(VolatileBag.keyStorage.getKey(name + "-IV", KeyStorage.defaultPassword));
			} catch (UnrecoverableEntryException e1) {
				logger.warning("Unrecoverable exception caught: " + e1.getMessage());
				SOptions.showError(null, "PROSecurity - Error",
						"Incorrect password for Safe: " + name + ". \nTry again.");
			} catch (NullPointerException e1) {
				logger.severe("NullPointerException caught: " + e1.getMessage());
				SOptions.showError(null, "PROSecurity- Runtime Error",
						"An runtime error has occured.\nError Code: 401");
			} catch (IOException e1) {
				logger.severe("IOException caught: " + e1.getMessage());
				SOptions.showError(null, "PROSecurity- Runtime Error",
						"An runtime error has occured.\nError Code: 402");
			}
		} else {
			do {
				wrongPassword = false;
				logger.info("Requesting password: ");
				Display display = new Display();
				Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.CLOSE);
				shell.setText("PROSecurity - Unlock " + name);
				GridLayout layout = new GridLayout();
				layout.numColumns = 2;
				layout.marginHeight = 20;
				layout.marginWidth = 20;
				shell.setLayout(layout);

				new Label(shell, SWT.NONE).setText("Enter password: ");
				Text passwordBox = new Text(shell, SWT.PASSWORD);

				SelectionAdapter submitAdapter = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						if (passwordBox.getText().isEmpty()) {
							SOptions.showError(shell, "PROSecurity - Error",
									"Password can not be empty.! Please enter password.");
							return;
						}
						password = passwordBox.getText();
						shell.dispose();
					}
				};

				passwordBox.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.CR) {
							submitAdapter.widgetSelected(null);
						}
					}
				});

				GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
				gridData.horizontalSpan = 2;
				Button submit = new Button(shell, SWT.PUSH);
				submit.setText(" Submit ");
				submit.setLayoutData(gridData);
				submit.addSelectionListener(submitAdapter);

				shell.addListener(SWT.Close, new Listener() {
					@Override
					public void handleEvent(Event event) {
						password = null;
					}
				});

				shell.pack();
				Rectangle screenSize = display.getPrimaryMonitor().getBounds();
				shell.setLocation((screenSize.width - shell.getBounds().width) / 2,
						(screenSize.height - shell.getBounds().height) / 2);
				shell.open();

				// Wait for dispose of the shell
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
				// display.dispose();

				if (password == null || password.isEmpty()) {
					logger.warning("Did not recieve password");
					return;
				}

				try {
					secretKey = VolatileBag.keyStorage.getKey(name, password);
					ivNums = CryptOperations.toByte(VolatileBag.keyStorage.getKey(name + "-IV", password));
				} catch (UnrecoverableEntryException e1) {
					logger.warning("Unrecoverable exception caught: " + e1.getMessage());
					return;
				} catch (NullPointerException e1) {
					logger.severe("NullPointerException caught: " + e1.getMessage());
					return;
				} catch (IOException e1) {
					logger.severe("IOException caught: " + e1.getMessage());
					return;
				}
			} while (wrongPassword);

			logger.fine("Correct password recieved. Decrypting file");

		}
		String inputFile = resPath + "\\" + getSafeFileName() + ".prhd";
		String outputFile = tempPath + "\\" + getSafeFileName() + ".vhd";
		CryptOperations.doOperation(Cipher.DECRYPT_MODE, secretKey, inputFile, outputFile, ivNums);
		logger.fine("decrypting success. Attaching disk");

		DiskOperations.attachDisk(getSafeFileName());
		setUnlocked();
		logger.finest("Unlocking safe " + name + " complete. Starting watch thread");

		if (lockType != PWD_ONLY)
			startWatchThread();
		VolatileBag.safeOngoing = false;

	}

	/**
	 * Locks the safe
	 */
	public void lock() {
		VolatileBag.safeOngoing = true;
		// stop watch thread
		stopWatchThread();
		System.out.println("Trying to lock safe: " + name + " lockType: " + lockType);
		// Detach
		DiskOperations.dettachDisk(getSafeFileName());
		setLocked();

		TrayOperations.displayMessage("PROSecurity - Safe Secured", "Safe " + name + " has been successfully locked.!",
				TrayIcon.MessageType.INFO);

		logger.info("Encrypting disk");
		String outputFile = resPath + "\\" + getSafeFileName() + ".prhd";
		String inputFile = tempPath + "\\" + getSafeFileName() + ".vhd";
		CryptOperations.doOperation(Cipher.ENCRYPT_MODE, secretKey, inputFile, outputFile, ivNums);
		logger.fine("Encrypting file successful. Deleting file");

		logger.info("deleting .vhd file");
		File safeFile = new File(inputFile);
		safeFile.delete();

		if (safeFile.exists()) {
			System.out.println("Deleting safe file failed");
		}
		VolatileBag.safeOngoing = false;
		logger.finest("Locking safe successful");
	}

	/**
	 * Reads the .sdat file from Res folder and initializes a object of this class
	 * 
	 * @param name
	 *            String containg name of the Safe without extension
	 * @return Safe object loaded from file
	 * @throws IOException
	 *             If failed to load from file
	 * @throws FileNotFoundException
	 *             If Specified sdat file not found in Res folder
	 */
	public static Safe deserial(String name) throws IOException, FileNotFoundException {
		try {
			File safeDat = new File(resPath + "\\" + name + safeExt);
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(safeDat));
			SafeData dat = (SafeData) ois.readObject();
			ois.close();
			return new Safe(dat);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	/**
	 * Starts watch thread for the safe
	 */
	public void startWatchThread() {
		if (!watchRunning) {
			cancel = false;
			watchRunning = true;
			watchThread = new Thread(watchRun);
			watchThread.start();
		}
	}

	/**
	 * Stops watch thread of this safe
	 */
	public void stopWatchThread() {
		try {
			if (watchThread.isAlive()) {
				System.out.println("Starting watch thread");
				cancel = true;
				watchRunning = false;
				watchThread.interrupt();
			}
		} catch (Exception e) {

		}
	}

	Runnable watchRun = new Runnable() {

		@Override
		public void run() {
			logger.info("Initiating WatchThread for safe: " + name);
			System.out.println("Starting watch thread...");
			while (!cancel && !Thread.currentThread().isInterrupted()) {
				try {
					System.out.println("WatchThread checking " + name);
					if (!BTOperations.checkRange(mac, service)) {
						logger.info(name + ": WatchThread: BT Device not found, locking device.");
						lock();
						VolatileBag.updateSafe();
					}
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.warning("Safe WatchThread interrupted: " + e.getMessage());
				}
			}
			System.out.println("WatchThread exiting...");
		}
	};

}
