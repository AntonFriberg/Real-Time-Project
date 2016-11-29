package skeleton.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

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

	public ClientSend(String server, int port, ClientMonitor monitor) {
		this.server = server;
		this.port = port;
		this.monitor = monitor;
	}

	public void run() {
		while(true){
			try {
				int newCommand = monitor.getCommand();
				sendCommand(newCommand);
			} catch (Exception e) {
				System.out.println("Connection Error");
			}			
		}
	}

	private void sendCommand(int newCommand) throws UnknownHostException, IOException {
		sock = new Socket(server, port);
		is = sock.getInputStream();
		os = sock.getOutputStream();

		if(newCommand == ClientMonitor.IDLE_MODE){
			putLine(os, MOTION_OFF); // Start the transmission of pictures
			putLine(os, ""); // The request ends with an empty line
		} else if(newCommand == ClientMonitor.MOVIE_MODE){
			putLine(os, MOTION_ON); // Start the transmission of pictures
			putLine(os, ""); // The request ends with an empty line
		} else {
			putLine(os, DISCONNECT); // Start the transmission of pictures
			putLine(os, ""); // The request ends with an empty line
		}
		
		// Read the first line of the response (status line)
		String responseLine;
		responseLine = getLine(is);
		System.out.println("HTTP server says '" + responseLine + "'.");

		// Ignore the following header lines up to the final empty one.
		do {
			responseLine = getLine(is);
		} while (!(responseLine.equals("")));

		
		os.flush();
		sock.close();
	}
	
	/**
	 * Read a line from InputStream 's', terminated by CRLF. The CRLF is not
	 * included in the returned string.
	 */
	private static String getLine(InputStream s) throws IOException {
		boolean done = false;
		String result = "";

		while (!done) {
			int ch = s.read(); // Read
			if (ch <= 0 || ch == 10) {
				// Something < 0 means end of data (closed socket)
				// ASCII 10 (line feed) means end of line
				done = true;
			} else if (ch >= ' ') {
				result += (char) ch;
			}
		}

		return result;
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
