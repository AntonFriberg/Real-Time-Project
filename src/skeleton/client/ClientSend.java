package skeleton.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author Olof Rubin and Erik Andersson
 *
 */
public class ClientSend extends Thread {
	private Socket sock;
	private InputStream is;
	private OutputStream os;
	private static final byte[] CRLF = { 13, 10 };
	private int port;
	private String server;
	private ClientMonitor monitor;
	
	public static final String MOTION_OFF = "CMI ";
	public static final String MOTION_ON = "CMM ";
	public static final String DISCONNECT = "DSC ";
	public static final String AUTO_MODE = "AUT ";
	public static final String MANUAL_MODE = "MAN ";
	private	int cameraID;
	
	public ClientSend(String server, int port, ClientMonitor monitor, int cameraID) {
		this.server = server;
		this.port = port;
		this.monitor = monitor;
		this.cameraID = cameraID;
	}

	public void run() {
		try {
			sendCommand();
		} catch (Exception e) {
			System.out.println("Connection Error");
		}

	}

	private void sendCommand() throws UnknownHostException, IOException, InterruptedException {
		while (true) {
			monitor.waitForConnect();
			sock = new Socket(server, port);
			is = sock.getInputStream();
			os = sock.getOutputStream();
			System.out.println("Client sending at " + port);
	
			while (sock.isConnected()) {
				int newCommand = monitor.getCommand(cameraID);
				System.out.println("Sending + : ");
				if (newCommand == ClientMonitor.MOTION_OFF) {
					System.out.println(MOTION_OFF);
					putLine(os, MOTION_OFF); // Start the transmission of pictures
					putLine(os, ""); // The request ends with an empty line
				} else if (newCommand == ClientMonitor.MOTION_ON) {
					System.out.println(MOTION_ON);
					putLine(os, MOTION_ON); // Start the transmission of pictures
					putLine(os, ""); // The request ends with an empty line
				} else if (newCommand == ClientMonitor.DISCONNECT){
					System.out.println(DISCONNECT);
					putLine(os, DISCONNECT); // Start the transmission of pictures
					putLine(os, ""); // The request ends with an empty line
				} else if (newCommand == ClientMonitor.AUTO_MODE){
					System.out.println(AUTO_MODE);
					putLine(os, AUTO_MODE); // Start the transmission of pictures
					putLine(os, ""); // The request ends with an empty line
				} else {
					System.out.println(MANUAL_MODE);
					putLine(os, MANUAL_MODE); // Start the transmission of pictures
					putLine(os, ""); // The request ends with an empty line
				}
				os.flush();
			}
			try {
				is.close();
				os.close();
				sock.close();
			}catch (IOException e) {
	            //System.out.println("Caught exception " + e);
	        }finally {
			
	        }
		}
	}

	/**
	 * Send a line on OutputStream 's', terminated by CRLF. The CRLF should not
	 * be included in the string str.
	 */
	private static void putLine(OutputStream s, String str) throws IOException {
		s.write(str.getBytes());
		s.write(CRLF);
	}

}
