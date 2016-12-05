package skeleton.demo;

import skeleton.client.CameraController;
import skeleton.server.proxy.Server;

//import skeleton.server.fake.Server;
//import skeleton.server.real.Server;

public class ProxyTest {

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("Wrong number of arguments: (\"argus-N1.student.lth.se\", send port 1, \"argus-N2.student.lth.se\" send port 2");
            System.exit(0);
        }
        String proxy1 = args[0];
        int port1 = Integer.parseInt(args[1]);
        String proxy2 = args[2];
        int port2 = Integer.parseInt(args[3]);
    	int sendPorts[] = {port1-1, port2-1};
        int receivePorts[] = {port1, port2};
        Server cam1 = new Server(proxy1, sendPorts[0], receivePorts[0]);
        Server cam2 = new Server(proxy2, sendPorts[1], receivePorts[1]);
        //Server cam3 = new Server("argus-3.student.lth.se", sendPorts[2], receivePorts[2]);
        cam1.start();
        cam2.start();
        //cam3.start();
        CameraController.main(new String[]{"localhost", Integer.toString(port1-1), Integer.toString(port2-1),
                                           Integer.toString(port1), Integer.toString(port2)});
    }
}