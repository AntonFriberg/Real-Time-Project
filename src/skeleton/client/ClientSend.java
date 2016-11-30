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
		try {
			sendCommand();
		} catch (Exception e) {
			System.out.println("Connection Error");
		}

	}

	private void sendCommand() throws UnknownHostException, IOException, InterruptedException {
		sock = new Socket(server, port);
		is = sock.getInputStream();
		os = sock.getOutputStream();

		while (sock.isConnected()) {
			int newCommand = monitor.getCommand();
			System.out.println("Sending + : ");
			if (newCommand == ClientMonitor.IDLE_MODE) {
				System.out.println(MOTION_OFF);
				putLine(os, MOTION_OFF); // Start the transmission of pictures
				putLine(os, ""); // The request ends with an empty line
			} else if (newCommand == ClientMonitor.MOVIE_MODE) {
				System.out.println(MOTION_OFF);
				putLine(os, MOTION_ON); // Start the transmission of pictures
				putLine(os, ""); // The request ends with an empty line
			} else {
				System.out.println(MOTION_OFF);
				putLine(os, DISCONNECT); // Start the transmission of pictures
				putLine(os, ""); // The request ends with an empty line
			}
			os.flush();
		}
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
