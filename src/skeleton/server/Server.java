package skeleton.server;

/**
 * Created by Anton Friberg on 04/12/16.
 */
public class Server {
    private ServerReceive receive;
    private ServerSend send;
    private CameraMonitor cm;
    private CameraHandler ch;

    public Server(int receivePort, int sendPort) {
        cm = new CameraMonitor(sendPort);
        send = new ServerSend(receivePort, cm);
        receive = new ServerReceive(sendPort, cm);
        ch = new CameraHandler(cm);
    }

    public void start() {
        send.start();
        receive.start();
        ch.start();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Faulty argument count (needs two): Receive Port, Send Port");
            System.exit(0);
        }

        System.out.println("Receive port: " + args[0]);
        System.out.println("Send port: " + args[1]);

        Server s = new Server((Integer.parseInt(args[0])),
                Integer.parseInt(args[1]));
        s.start();
    }
}
