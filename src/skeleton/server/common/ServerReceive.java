package skeleton.server.common;

import skeleton.server.common.CameraMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Anton Friberg and Joakim Magnusson on 11/15/16.
 * Thread that listens for commands
 */
public class ServerReceive extends Thread {
	/**
	 * Receives clients command to change camera mode and disconnect.
     * Each camera has one receive thread and communicates with the
     * client via byte array and expects CMM for motion, CMI for idle
     * and DSC for disconnect.
	 */
    private static final String MOTION_MODE = "CMM ";
    private static final String IDLE_MODE = "CMI ";
    private static final String DISCONNECT = "DSC ";
    private static final String AUTO_MODE = "AUT ";
    private static final String MANUAL_MODE = "MAN ";
	private static final byte[] CRLF = { 13, 10 }; //
	private int port;
	private CameraMonitor cm;

	public ServerReceive(int port, CameraMonitor cm) {
		this.port = port;
		this.cm = cm;
	}

	public void run() {
		// Prepare connection
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("HTTP receiving server operating at port: " + port + ".");

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
                 * also listens for clients request to disconnect the connection
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

				    System.out.println("HTTP request '" + request);

                    // Interpret the request. Complain about everything but GET.
                    // Ignore the file name.
                    if (request.substring(0, 4).equals(MOTION_MODE)) {
                        // Got a CMM request (Change Mode Motion)
                        // or our camera detected motion, respond
                        // by changing the mode and frame rate to
                        // motion.
                        System.out.println("MOTION ACTIVATE");
                        cm.activateMotion(true);
                    } else if (request.substring(0, 4).equals(IDLE_MODE)) {
                        // Got a CMI request (Change Mode Idle),
                        // respond by changing mode and frame
                        // rate to idle.
                    	System.out.println("IDLE ACTIVATE");
                        cm.activateMotion(false);
                    } else if (request.substring(0, 4).equals(DISCONNECT)){ // servern ska aldrig k�ra disconect!!!!
                        // Got a DSC request (Disconnect)
                        // respond by propagating the closure
                        // of sockets via the monitor
                        //System.out.println("DISCONNECT SOCKETS");
                        //System.out.println("Received DSC");
                        //cm.disconnect();
                    } else if (request.substring(0, 4).equals(AUTO_MODE)) {
                        // Got a CMI request (Change Mode Idle),
                        // respond by changing mode and frame
                        // rate to idle.
                    	System.out.println("AUTO ACTIVATE");
                        cm.setAuto(true);
                    } else if (request.substring(0, 4).equals(MANUAL_MODE)) {
                        // Got a CMI request (Change Mode Idle),
                        // respond by changing mode and frame
                        // rate to idle.
                    	System.out.println("MANUAL ACTIVATE");
                        cm.setAuto(false);
                    }
                }
				os.flush(); // Flush any remaining content
				receiveSocket.close(); // Disconnect from the client
			} catch (SocketException e) {
				System.out.println("Caught exception " + e);
			} catch (IOException e) {
                System.out.println("Caught exception " + e);
            } catch (StringIndexOutOfBoundsException e) {
                System.out.println("Received strange request, recovering");
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
}
