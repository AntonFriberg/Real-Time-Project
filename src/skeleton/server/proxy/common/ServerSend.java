package skeleton.server.proxy.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Anton Friberg and Joakim Magnusson on 11/15/16.
 * Thread that sends images
 */
public class ServerSend extends Thread{
    /**
     * Sends the image command, data, timestamp and current mode
     * over the network via byte arrays. There is one send
     * thread for each camera connected to the client.
     */

    private static final String START_CONNECTION = "SRT ";
    private ServerSocket serverSocket;
    private int port;
    private CameraMonitor cm;

    public ServerSend(int port, CameraMonitor cm) {
        this.port = port;
        this.cm = cm;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            //serverSocket.setSoTimeout(180000);  // Socket will timeout after 18 s of inactivity
            System.out.println("HTTP sending server operating at port: " + port + ".");

            // initialize empty network objects
            Socket server = null;
            InputStream is = null;
            OutputStream os = null;

            // Main loop
            while (true) {
                try {
                    // The 'accept' method waits for a client to connect, then
                    // returns a socket connected to that client.
                    server = serverSocket.accept();
                    System.out.println("Accepted connection: " + server);

                    // Tell the monitor that we are connected
                    cm.connect();

                    // The socket is bi-directional. It has an input stream to read
                    // from and an output stream to write to. The InputStream can
                    // be read from using read(...) and the OutputStream can be
                    // written to using write(...). However, we use our own
                    // getLine/putLine methods below.
                    is = server.getInputStream();
                    os = server.getOutputStream();

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
                            + "' received by ServerSend.");

                    // Start sending images after start command is received.
                    // Continue sending while the connection is active
                    if (request.substring(0, 4).equals(START_CONNECTION)) {
                        // Until the client tells us to disconnect
                        while (cm.connected()) {
                            cm.sendImage(os);
                            os.flush();
                        }
                    }

                    os.flush();                      // Flush any remaining content

                } catch (IOException e) {
                    System.out.println("Caught exception " + e);
                } catch (Exception e) {
                    System.out.println("Caught exception " + e);
                } finally {
                    // Clean up remaining connections before listening one
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
            System.out.println("Socket exception found, recovering.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("Received strange request, recovering");
        } finally {
            // Clean up connection at shutdown
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
}
