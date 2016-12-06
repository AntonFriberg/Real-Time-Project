package skeleton.demo;

import skeleton.client.CameraController;
//import skeleton.server.proxy.Server;
//import skeleton.server.fake.Server;
import skeleton.server.real.Server;

import static java.lang.Thread.sleep;

public class RealTest {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Wrong number of arguments: (send port 1, send port 2");
            System.exit(0);
        }
        int port1 = Integer.parseInt(args[1]);
        int port2 = Integer.parseInt(args[3]);
        int sendPorts[] = {port1-1, port2-1};
        int receivePorts[] = {port1, port2};

        Server cam1 = new Server(sendPorts[0], receivePorts[0]);
        Server cam2 = new Server(sendPorts[1], receivePorts[1]);

        cam1.start();
        cam2.start();

        CameraController.main(new String[]{"localhost", Integer.toString(port1-1), Integer.toString(port2-1),
                Integer.toString(port1), Integer.toString(port2)});
    }
}