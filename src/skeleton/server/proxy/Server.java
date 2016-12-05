package skeleton.server.proxy;

import skeleton.server.proxy.common.CameraMonitor;
import skeleton.server.proxy.common.ServerReceive;
import skeleton.server.proxy.common.ServerSend;
import skeleton.server.proxy.common.CameraHandler;

/**
 * Created by Anton Friberg and Joakim Magnusson on 04/12/16.
 *
 * Umbrella class that collects the monitor and threads for
 * the server side.
 */
public class Server {
    private ServerReceive receive;
    private ServerSend send;
    private CameraMonitor cm;
    private CameraHandler ch;

    /**
     * Constructor that creates the monitor and threads
     * @param receivePort
     * The client's receive port
     * @param sendPort
     * The client's send port
     */
    public Server(String proxy, int receivePort, int sendPort) {
        cm = new CameraMonitor(proxy, sendPort);
        send = new ServerSend(receivePort, cm);
        receive = new ServerReceive(sendPort, cm);
        ch = new CameraHandler(cm);
    }

    /**
     * The method to start the threads
     */
    public void start() {
        send.start();
        receive.start();
        ch.start();
    }

    /**
     * Main method for ability to run Server on camera
     * independently of the client.
     * @param args
     * Two arguments [Client's Receive Port, Client's Send Port]
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Faulty argument count (needs two): Receive Port, Send Port");
            System.exit(0);
        }

        System.out.println("Receive port: " + args[0]);
        System.out.println("Send port: " + args[1]);

        Server s = new Server(args[0],(Integer.parseInt(args[1])),
                Integer.parseInt(args[2]));
        s.start();
    }
}
