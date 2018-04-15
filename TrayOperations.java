import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.eclipse.swt.widgets.Shell;

public class TrayOperations {
	public final static String loggerName = "default.runtime";
	static Logger logger;
	private static ArrayList<String> errorMessages = new ArrayList<String>();
	private static boolean isSupported;
	private static boolean defaultSafeSet = false;
	private static Safe defaultSafe;
	private static MenuItem safeItem, loginItem, settingsItem, aboutItem, exitItem;
	private static TrayIcon trayIcon;
	private static URL url;
	private static Image greenShield, yellowShield;

	/**
	 * Initializes logger and adds tray Icon
	 */
	public static void init() {
		logger = Logger.getLogger(loggerName);
		logger.info("DiskOperations Initialized");

		if (SystemTray.isSupported()) {
			try {
				isSupported = true;
				// initialize Pop-up menu
				PopupMenu menu = new PopupMenu();

				safeItem = new MenuItem("Safes");
				loginItem = new MenuItem("Auto Lock");
				settingsItem = new MenuItem("Settings");
				aboutItem = new MenuItem("About PROSecurity");
				exitItem = new MenuItem("Exit");

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
				url = System.class.getResource("/images/green-Shield.png");
				greenShield = Toolkit.getDefaultToolkit().getImage(url);
				url = System.class.getResource("/images/yellow-Shield.png");
				yellowShield = Toolkit.getDefaultToolkit().getImage(url);
				trayIcon = new TrayIcon(greenShield, "PRO Security", menu);
				// TODO Add ActionListener to TrayIcon

				SystemTray.getSystemTray().add(trayIcon);
				logger.fine("Tray Icon has been initialized");
			} catch (AWTException e) {
				logger.severe("AWT Exception caught: " + e.getMessage());
			}
		} else {
			isSupported = false;
			logger.warning("System does not support tray Icon");
			SOptions.showError(new Shell(), "PROSecurity - Error",
					"Your system does not support tray icon. PROSecurity will continue to work without it.");
		}
	}

	/**
	 * Returns the overall status of the application Green or Yellow
	 * 
	 * @return True if Green, else false
	 */
	public static boolean getStatus() {
		if (errorMessages.isEmpty())
			return true;
		return false;
	}

	/**
	 * Sets the application overall status to Yellow: Not Ok Adds the message to
	 * tray tooltip text
	 * 
	 * @param message
	 *            String message to be displayed in the tool tip of tray icon
	 */
	public static void toYellow(String message) {
		errorMessages.add(message);
		if(isSupported) {
			if (errorMessages.size() == 1) {
				trayIcon.setImage(yellowShield);
				trayIcon.setToolTip("PROSecurity | " + message);
			} else {
				trayIcon.setToolTip("PROSecurity | More than one action required ");
			}
		}
	}

	/**
	 * Sets the application overall status to green
	 * 
	 * @param message
	 *            String containing tray icon message used with function toYellow
	 */
	public static void toGreen(String message) {
		errorMessages.remove(message);
		if(isSupported) {
			if (errorMessages.size() == 0) {
				trayIcon.setImage(greenShield);
				trayIcon.setToolTip("PROSecurity");
			} else if (errorMessages.size() == 1) {
				trayIcon.setToolTip("PROSecurity | " + errorMessages.get(0));
			} else {
				trayIcon.setToolTip("PROSecurity | More than one action required");
			}
		}
	}

	/**
	 * Show the Tray Icon pop up message with specified title, message and type
	 * 
	 * @param title
	 *            String containing title of the Tray Icon message
	 * @param message
	 *            String containing body of the Tray Icon message
	 * @param messageType
	 *            Type of the message to be displayed
	 */
	public static void displayMessage(String title, String message, MessageType messageType) {
		if(isSupported) {
			trayIcon.displayMessage(title, message, messageType);
		}
	}

	/**
	 * Checks whether default safe has been set
	 * @return True, If set. Else false
	 */
	public static boolean isDefaultSafeSet() {
		return defaultSafeSet;
	}
	
	/** Sets the default safe, when double clicked on tray icon, this safe's unlock process will be initiated.
	 * Most recently set safe will be used.
	 * @param safe Safe to be set as default
	 */
	public static void setDefaultSafe(Safe safe) {
		if(!defaultSafeSet) 
			defaultSafeSet = true;
		defaultSafe = safe;
	}

	/**
	 * Returns the default safe
	 * @return 
	 * 		Most recently set Safe as default, if set. Else null
	 */
	public static Safe getDefaultSafe() {
		return defaultSafeSet?defaultSafe:null;
	}

}
