package skeleton.demo;

import skeleton.client.CameraController;
import skeleton.server.fake.Server;

//import skeleton.server.proxy.Server;
//import skeleton.server.fake.Server;

public class FakeTest {

    public static void main(String[] args) {

        String proxy1 = "argus-1.student.lth.se";
        String proxy2 = "argus-2.student.lth.se";
        int port1 = 6078;
        int port2 = 6080;
        int sendPorts[] = {port1-1, port2-1};
        int receivePorts[] = {port1, port2};

        Server cam1 = new Server(proxy1, sendPorts[0], receivePorts[0]);
        Server cam2 = new Server(proxy2, sendPorts[1], receivePorts[1]);

        cam1.start();
        cam2.start();

        CameraController.main(new String[]{"localhost", Integer.toString(port1-1), Integer.toString(port2-1),
                Integer.toString(port1), Integer.toString(port2)});
    }
}