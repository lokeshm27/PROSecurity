import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Properties;
import java.util.logging.Logger;

public class ServerThread extends Thread {
	
	public final static String loggerName = "default.runtime";

	static Logger logger;
	public int port = 8912;
	public static String rootPath = System.getenv("LocalAppData") + "\\PROSecurity";
	public static String resPath = rootPath + "\\Res";
	public static String tempPath = rootPath + "\\Temp";
	public ServerSocket server;
	
	Properties config;
	InputStream input;
	
	public ServerThread() {
		logger = Logger.getLogger(loggerName);
		logger.info("Server Thread initialized");
	}
	
	
	public void run() {
		try {
			// reading port from config file
			config = new Properties();
			input = new FileInputStream(rootPath + "\\config.ini");
			config.load(input);
			
			port = new Integer(config.getProperty("Port", Integer.toString(port)));
			logger.info("Port number read: " + port);
			
			server = new ServerSocket(port);
			
			boolean interrupted = false;
			while(!interrupted) {
				try {
					new ProcessThread(server.accept(), server).start();
				} catch (SocketException e) {
					// Thread interrupted
					interrupted = true;
				}
			}
		} catch (IOException e) {
			logger.warning("IOException caught: " + e.getMessage());
		}
	}
	
	
	/*
	 * Closes server thread and ends ServerThread
	 */
	public void close() {
		logger.info("Closing ServerThread");
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
