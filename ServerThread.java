import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Properties;

public class ServerThread extends Thread {
	
	public int port = 8912;
	public static String rootPath = System.getenv("LocalAppData") + "\\PROSecurity";
	public static String resPath = rootPath + "\\Res";
	public static String tempPath = rootPath + "\\Temp";
	public ServerSocket server;
	
	Properties config;
	InputStream input;
	
	public void run() {
		try {
			// reading port from config file
			config = new Properties();
			input = new FileInputStream(rootPath + "\\config.ini");
			config.load(input);
			
			port = new Integer(config.getProperty("Port", Integer.toString(port)));
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
			e.printStackTrace();
		}
	}
}
