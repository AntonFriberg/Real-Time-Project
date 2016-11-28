package skeleton.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientReceive extends Thread {
	private Socket sock;
	private InputStream is;
	private OutputStream os;
	private static final byte[] CRLF = { 13, 10 };
	private byte[] jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
	private byte[] timeStamp = new byte[AxisM3006V.TIME_ARRAY_SIZE];
	private byte[] motionDetect = new byte[1];
	private int port;
	private String server;
	private ClientMonitor monitor;

	public ClientReceive(String server, int port, ClientMonitor monitor) {
		this.port = port;
		this.server = server;
		this.monitor = monitor;
	}

	public void run() {
		try {
			getImage();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getImage() throws IOException, InterruptedException {
		sock = new Socket(server, port);
		is = sock.getInputStream();
		os = sock.getOutputStream();

		putLine(os, "SRT /image.jpg HTTP/1.0"); // Start the transmission of
												// pictures
		putLine(os, ""); // The request ends with an empty line

		if (sock.isConnected()) {
			// Read the first line of the response (status line)
			String responseLine;
			responseLine = getLine(is);
			System.out.println("HTTP server says '" + responseLine + "'.");

			// Ignore the following header lines up to the final empty one.
			do {
				responseLine = getLine(is);
			} while (!(responseLine.equals("")));

			byte[] receivedData = new byte[AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE + 1];
			int bytesRead = readData(receivedData.length, receivedData);
			
			byte[] receivedData2 = new byte[AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE + 1];

			int bytesRead2 = readData(receivedData.length, receivedData);


			if (bytesRead >= AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE + 1) {
				// Load the JPEG
				System.arraycopy(receivedData, 0, jpeg, 0, AxisM3006V.IMAGE_BUFFER_SIZE);

				// Read the Time
				System.arraycopy(receivedData, AxisM3006V.IMAGE_BUFFER_SIZE, timeStamp, 0, AxisM3006V.TIME_ARRAY_SIZE);

				// Read the Motion
				System.arraycopy(receivedData, AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE, motionDetect,
						0, 1);

				monitor.putImage(jpeg, timeStamp, motionDetect[0]);
			} else {
				// Something went wrong
			}
			os.flush();
		}
		sock.close();

	}

	private int readData(int bufferSize, byte[] container) throws IOException {
		// Stores the bytes in the unspecified container
		int bytesRead = 0;
		int bytesLeft = bufferSize;
		int status;
		// We have to keep reading until -1 (meaning "end of file") is
		// returned. The socket (which the stream is connected to)
		// does not wait until all data is available; instead it
		// returns if nothing arrived for some (short) time.
		do {
			status = is.read(container, bytesRead, bytesLeft);
			// The 'status' variable now holds the no. of bytes read,
			// or -1 if no more data is available
			if (status > 0) {
				bytesRead += status;
				bytesLeft -= status;
			}
		} while (status >= 0);		
		System.out.println("Received data (" + bytesRead + " bytes).");
		return bytesRead;
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
