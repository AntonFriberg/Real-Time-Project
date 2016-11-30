package skeleton.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Anton Friberg and Joakim Magnusson on 11/15/16.
 */
public class ServerReceive extends Thread {
	/**
	 * Receives changes to the current camera mode. Each camera has one receive
	 * thread and communicates with the client via byte array and expects CMM
	 * for motion and CMI for idle.
	 */

	private Socket sock;
	private InputStream is;
	private OutputStream os;
	private static final byte[] CRLF = { 13, 10 };
	private int receivePort;
	private String client;
	private CameraMonitor cm;

	public ServerReceive(int port, String client, CameraMonitor cm) {
		this.receivePort = port;
		this.client = client;
		this.cm = cm;
	}

	public void run() {
		// Prepare connection
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(receivePort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("HTTP server operating at port " + receivePort + ".");

		while (true) {
			try {
				// The 'accept' method waits for a client to connect, then
				// returns a socket connected to that client.
				Socket receiveSocket = serverSocket.accept();
                // Tell the monitor that we are connected
				cm.connect();

				// The socket is bi-directional. It has an input stream to read
				// from and an output stream to write to. The InputStream can
				// be read from using read(...) and the OutputStream can be
				// written to using write(...). However, we use our own
				// getLine/putLine methods below.
				InputStream is = receiveSocket.getInputStream();
				OutputStream os = receiveSocket.getOutputStream();

                /**
                 * Main loop that listens for commands to change mode or if camera has detected motion
                 */
                while(cm.connected()) {
				    // Read the request
				    String request = getLine(is);

				    // The request is followed by some additional header lines,
				    // followed by a blank line. Those header lines are ignored.
				    String header;
				    boolean cont;
				    do {
				    	header = getLine(is);
				    	cont = !(header.equals(""));
				    } while (cont);

				    System.out.println("HTTP request '" + request + "' received.");

                    // Interpret the request. Complain about everything but GET.
                    // Ignore the file name.
                    boolean motion = cm.motionDetected();
                    if (motion || request.substring(0, 4).equals("CMM ")) {
                        /**
                         * Got a CMM request (Change Mode Motion)
                         * or our camera detected motion, respond
                         * by changing the mode and frame rate to
                         * motion.
                         */
                        System.out.println(request);
                        System.out.println("MOTION ACTIVATE" + motion);
                        cm.activateMotion(true);
                    } else if (request.substring(0, 4).equals("CMI ")) {
                        /**
                         * Got a CMI request (Change Mode Idle),
                         * respond by changing mode and frame
                         * rate to idle.
                         */
                    	System.out.println("IDLE ACTIVATE");
                        cm.activateMotion(false);
                    }else if (request.substring(0, 4).equals("DSC ")){
                        /**
                         * Got a DSC request (Disconnect)
                         * respond by propagating the closure
                         * of sockets via the monitor
                         */
                        System.out.println("DISCONNECT SOCKETS");
                        System.out.println("Received DSC");
                        cm.disconnect();
                    } else {
                        // Got some other request. Respond with an error message.
//                        putLine(os, "HTTP/1.0 501 Method not implemented");
//                        putLine(os, "Content-Type: text/plain");
//                        putLine(os, "");
//                        putLine(os, "No can do. Request '" + request + "' not understood.");
//
//                        System.out.println("Unsupported HTTP request!");
                    }
                    
                    try {
                        sleep(100); // Should perhaps change to wait inside monitor (limits cpu time)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



				os.flush(); // Flush any remaining content
				receiveSocket.close(); // Disconnect from the client
			} catch (IOException e) {
				System.out.println("Caught exception " + e);
			}
		}
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
