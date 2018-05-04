import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ProcessThread extends Thread {
	
	public final static String loggerName = "default.runtime";

	static Logger logger;
	boolean useSocket = false;
	Socket socket;
	ServerSocket server;
	String message;

	
	/*
	 * Constructor:
	 * 
	 * @param socket: Socket from which message has to be recieved
	 * 
	 * @param server: ServerSocket used by ServerThread. This ServerSocket is used
	 * to interrupt the thread in case of close operation
	 */
	public ProcessThread(Socket socket, ServerSocket server) {
		this.socket = socket;
		this.server = server;
		useSocket = true;
		logger = Logger.getLogger(loggerName);
		logger.info("Process Thread initialized with socket");
	}

	
	/*
	 * Constructor:
	 * 
	 * @param: String containing message recieved from client and to be processed
	 */

	public ProcessThread(String msg) {
		this.message = msg;
		logger = Logger.getLogger(loggerName);
		logger.info("Process Thread initialized with message: " + msg);
	}

	
	/*
	 * start process thread
	 */
	public void run() {
		try {
			if (useSocket) {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

				String msg = (String) ois.readObject();
				logger.info("Recieved message from socket: " + msg);
				
				// Process the message
				this.message = msg;
				process();

				oos.writeObject(new String("PROSecurity|ACK"));
				oos.flush();
				oos.close();
				ois.close();
			} else {
				process();
			}
		} catch (IOException e) {
			logger.warning("IOException caught: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.warning("ClassNotFoundException caught: " + e.getMessage());
		}
	}
	
	
	/*
	 * Process message
	 */
	private void process() {
		logger.info("Processign message: " + message);
		
		//TODO Complete processing of message
		SOptions.showInformation(null, "PROSecurity - Message", "A Message recieved : " + this.message);		
		return;
	}
}
