package skeleton.demo;

import skeleton.client.CameraController;
import skeleton.server.proxy.Server;

import static java.lang.Thread.sleep;

public class SocketTest {

    public static void main(String[] args) {
     
    	int sendPorts[] = {6077, 6080, 6090};
        int receivePorts[] = {6078, 6081, 6091};
        Server cam1 = new Server("argus-1.student.lth.se", sendPorts[0], receivePorts[0]);
        Server cam2 = new Server("argus-2.student.lth.se", sendPorts[1], receivePorts[1]);
        Server cam3 = new Server("argus-3.student.lth.se", sendPorts[2], receivePorts[2]);
        cam1.start();
        cam2.start();
        cam3.start();
        CameraController.main(new String[]{"localhost", "6077", "6080", "6090","6078", "6081", "6091"});

        while (true) {
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }
    }
}