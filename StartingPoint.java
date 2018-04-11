import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Shell;

public class StartingPoint {
	
	public final static String loggerName = "default.runtime";

	static Logger logger;
	public static int port = 8912;
	public static String rootPath = System.getenv("LocalAppData") + "\\PROSecurity";
	public static String resPath = rootPath + "\\Res";
	public static String tempPath = rootPath + "\\Temp";
	static FileLock lock = null;
	static FileChannel channel = null;
	
	
	/*
	 * Starts application in either Server mode or Client mode
	 * 		Performs File Locking and Port Checking
	 */
	@SuppressWarnings("resource")

	public static void main(String args[]) {

		// Check if One-time initialization is required
		if (!new File(rootPath).exists()) {
			init();
		}

		File lockFile = new File(rootPath + "\\lock.tmp");
		try {
			if (!lockFile.exists()) {
				lockFile.createNewFile();
			}

			// Acquire lock
			channel = new RandomAccessFile(lockFile, "rw").getChannel();
			lock = channel.tryLock();

			if (lock == null) {
				// Instance already running
				clientMode(args);
			} else {

				// Add a shutdown hook to release lock
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							if (lock.isValid()) {
								lock.release();
								channel.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				serverMode(args);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*
	 * Creates folders required for operation: rootFolder, resFolder and tempFolder
	 */
	public static void init() {

		File rootFolder = new File(rootPath);
		File resFolder = new File(resPath);
		File tempFolder = new File(tempPath);
		File config = new File(rootFolder + "\\confog.ini");

		boolean ok = true;
		ok = rootFolder.mkdirs();
		ok = resFolder.mkdirs();
		ok = tempFolder.mkdirs();
		try {
			ok = config.createNewFile();
		} catch (IOException e) {
			ok = false;
		}

		if (!ok) {
			SOptions.showError(new Shell(), "PROSecurity - Initialization Error",
					"PROSecurity couldn't craete necessary folders in the path " + System.getenv("LocalAppData")
							+ " for operation. PROSecurity may require additional Previlages.");
			System.out.println("Error.!");
			System.exit(-1);
		}

		OutputStream output = null;
		try {
			Properties prop = new Properties();

			prop.setProperty("Port", Integer.toString(port));
			//TODO Other properties goes here.

			output = new FileOutputStream(config);
			prop.store(output, "Initialized config file with default values");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	
	/*
	 * Starts the operation in ServerMode
	 * 		Creates and runs all necessary threads and operations
	 * @param args[] String Array that contains the run time arguments
	 */
	public static void serverMode(String args[]) {
		File loginDatFile = new File(resPath + "\\login.dat");
		boolean loginConfigured = true;

		System.out.println("Server Mode");

		// Initialize logger
		logger = Logger.getLogger(loggerName);
		Handler fileHandler;
		try {
			fileHandler = new FileHandler(rootPath + "\\runtime.log", true);
			fileHandler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					Calendar date = Calendar.getInstance(TimeZone.getDefault());
					return record.getSequenceNumber() + "::" + record.getThreadID() + "::" + record.getSourceClassName()
							+ "::" + record.getSourceMethodName() + "::" + date.getTime() + "::" + record.getLevel()
							+ "::" + record.getMessage() + "\n";
				}
			});
			fileHandler.setLevel(Level.FINEST);
			logger.setUseParentHandlers(false);
			logger.addHandler(fileHandler);
			logger.setLevel(Level.ALL);
		} catch (SecurityException e) {
			// TODO
		} catch (IOException e) {
			// TODO
		}
		
		// Start thread to listen for messages
		logger.info("Initializing server thread");
		ServerThread serverThread = new ServerThread();
		serverThread.start();
		VolatileBag.serverThread = serverThread;
		//TODO Start Other threads
		
		//TODO Process current arguments
		String msg = getFormattedArgs(args);
		if(msg != null) {
			new ProcessThread(msg).start();
		}
		
		if(!loginDatFile.exists()) {
			logger.info("Auto Lock not configured");
			loginConfigured = false;
			VolatileBag.status = false;
		}

		// initialize tray Icon
		if (SystemTray.isSupported()) {
			try {
				// initialize Pop-up menu
				PopupMenu menu = new PopupMenu();

				MenuItem safeItem = new MenuItem("Safes");
				MenuItem loginItem = new MenuItem("Auto Lock");
				MenuItem settingsItem = new MenuItem("Settings");
				MenuItem aboutItem = new MenuItem("About PROSecurity");
				MenuItem exitItem = new MenuItem("Exit");

				// TODO Add action listener to MenuItems
				
				safeItem.addActionListener(new SafeActionListener());
				exitItem.addActionListener(new ExitActionListener());
				
				menu.add(safeItem);
				menu.add(loginItem);
				menu.addSeparator();
				menu.add(settingsItem);
				menu.add(aboutItem);
				menu.addSeparator();
				menu.add(exitItem);

				// Initialize Tray Image
				URL url = null;
				if (!loginConfigured) {
					// TODO Show message
					url = System.class.getResource("/images/yellow-Shield.png");
				} else {
					url = System.class.getResource("/images/green-Shield.png");
				}
				
				Image img = Toolkit.getDefaultToolkit().getImage(url);

				TrayIcon trayIcon = new TrayIcon(img, "PRO Security", menu);
				// TODO Add ActionListener to TrayIcon
				
				SystemTray.getSystemTray().add(trayIcon);
				logger.fine("Tray Icon has been initialized");
			} catch (AWTException e) {
				logger.severe("AWT Exception caught: " + e.getMessage());
			}
		} else {
			logger.warning("System does not support tray Icon");
			SOptions.showError(new Shell(), "PROSecurity - Error", "Your system does not support tray icon. PROSecurity will continue to work without it.");
		}
		try {
			serverThread.join();
			// TODO Wait for other threads
		} catch (InterruptedException e) {
			logger.severe("Main thread has been interrupted: " + e.getMessage());
		}
	}

	
	/*
	 * Starts operation in client mode
	 * 		Sends message to the servers and exists
	 *@param args[]: String Array that contains the run time arguments
	 */
	public static void clientMode(String args[]) {
		try {
			System.out.println("Clinet Mode");
			
			//Load properties
			Properties config = new Properties();
			InputStream input = new FileInputStream(rootPath + "\\config.ini");
			config.load(input);
			
			//read Port Number
			port = new Integer(config.getProperty("Port", Integer.toString(port)));
			
			//TODO Transform arguments
			String msg = "PROSecurity|Test Message";
			
			//Send message to server
			Socket socket = new Socket("localhost", port);
			socket.setSoTimeout(5000);
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(msg);
			
			// Read response
			socket.setSoTimeout(5000); // Wait for 5 seconds
			try {
				String response = (String) ois.readObject(); 
				oos.flush();
				oos.close();
				ois.close();
			
				// Check Response
				String[] responsePart = response.split("\\|");
				if(!responsePart[0].equalsIgnoreCase("PROSecurity") || !responsePart[1].equalsIgnoreCase("ACK")) {
					throw new SocketException("Invalid Response");
				}
				
				//TODO Remove
				System.out.println("Message Recieved by server: " + responsePart[1]);
			} catch (SocketException e) {
				// TODO Add option to go to settings
				
				SOptions.showError(new Shell(), "PROSecurity - Error", "Another instance of PRO Security is already running, but we are unable to establish communication with it or we recieved unexcepted response.\n"
						+ "This may be due to mismatch of Port address or some other application may be using the Port: " + port + "\n" 
						+ "Go to settings to change the Port Address");
			} catch (ClassNotFoundException e) {
				//TODO
				e.printStackTrace();
			} catch (Exception e) {
				//TODO
				e.printStackTrace();
			} finally {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/* TODO
	 * Formats the arguments
	 * @param args[]: Array of String containing arguments
	 * @return String formatted from the given array of String
	 */
	private static String getFormattedArgs(String args[]) {
		// TODO
		return null;
	}


}
