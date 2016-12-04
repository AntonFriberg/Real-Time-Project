package skeleton.demo;

import skeleton.client.CameraController;
import skeleton.client.ClientMonitor;
import skeleton.client.ClientReceive;
import skeleton.server.CameraHandler;
import skeleton.server.CameraMonitor;
import skeleton.server.ServerReceive;
import skeleton.server.ServerSend;
import skeleton.server.Server;

import static java.lang.Thread.sleep;

public class SocketTest {

    public static void main(String[] args) {
     
    	//int sendPorts[] = {6077, 6080};
        //int receivePorts[] = {6078, 6081};
//
        //Server cam1 = new Server(sendPorts[0], receivePorts[0]);
        //Server cam2 = new Server(sendPorts[1], receivePorts[1]);
        //cam1.start();
        //cam2.start();
        
        CameraController.main(new String[]{"localhost", "6077", "6080", "6078", "6081"});

        while (true) {
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }
    }
}