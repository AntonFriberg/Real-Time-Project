package skeleton.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Anton Friberg on 11/15/16.
 */
public class ServerSend extends Thread{
    /**
     * Packages the image data, timestamp and current mode into an ImageEvent and sends it
     * over the network via byte array over standardized protocol. There is one send
     * thread for each camera.
     */

    private ServerSocket serverSocket;
    private Socket server;
    private InputStream is;
    private OutputStream os;
    private static final byte[] CRLF = { 13, 10};
    private int port;
    private String client;
    private CameraMonitor cm;

    public ServerSend(int port, String client, CameraMonitor cm) {
        this.port = port;
        this.client = client;
        this.cm = cm;
        // Prepare connection
        serverSocket = null;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(180000);
            System.out.println("HTTP server operating at port " + port + ".");
            // Main loop
            while (true) {
                try {
                    // The 'accept' method waits for a client to connect, then
                    // returns a socket connected to that client.
                    server = serverSocket.accept();
                    System.out.println("Accepted connection: " + server);

                    // The socket is bi-directional. It has an input stream to read
                    // from and an output stream to write to. The InputStream can
                    // be read from using read(...) and the OutputStream can be
                    // written to using write(...). However, we use our own
                    // getLine/putLine methods below.
                    InputStream is = server.getInputStream();
                    OutputStream os = server.getOutputStream();

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

                    System.out.println("HTTP request '" + request
                            + "' received.");

                    if (request.substring(0, 4).equals("SRT ")) {
                        while (true) {
                            System.out.println("sending image");
                            cm.sendImage(os);
                            os.flush();
                        }
                    }

                    //while (sendSocket.isConnected()) {
                    //    cm.sendImage(os);
                    //}

                    os.flush();                      // Flush any remaining content
                } catch (IOException e) {
                    System.out.println("Caught exception " + e);
                } finally {
                    try {
                        if (is != null) is.close();
                        if (os != null) os.close();
                        if (server != null) server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Read a line from InputStream 's', terminated by CRLF. The CRLF is
     * not included in the returned string.
     */
    private static String getLine(InputStream s) throws IOException{
        boolean done = false;
        String result = "";

        while (!done) {
            int ch = s.read();  // Read
            if (ch <= 0 || ch == 10) {
                // Something < 0 means end of data (closed socket)
                // ASCII 10 (line feed) means end of line
                done = true;
            }
            else if (ch >= ' ') {
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
