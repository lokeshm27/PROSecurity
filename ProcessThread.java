import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessThread extends Thread {

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
	}

	/*
	 * Constructor:
	 * 
	 * @param: String containing message recieved from client and to be processed
	 */

	public ProcessThread(String msg) {
		this.message = msg;
	}

	public void run() {
		try {
			if (useSocket) {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

				String msg = (String) ois.readObject();

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
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void process() {
		//TODO Complete processing of message
		SOptions.showInformation("PROSecurity - Message", "A Message recieved : " + this.message);
		
		return;
	}
}
