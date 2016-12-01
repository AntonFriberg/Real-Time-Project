package skeleton.demo;

import skeleton.client.CameraController;
import skeleton.client.ClientMonitor;
import skeleton.client.ClientReceive;
import skeleton.server.CameraHandler;
import skeleton.server.CameraMonitor;
import skeleton.server.ServerReceive;
import skeleton.server.ServerSend;

public class SocketTest {

    public static void main(String[] args) {
     
    	int sendPorts[] = {6077, 6080};
        int receivePorts[] = {6078, 6081};
        CameraMonitor cm1 = new CameraMonitor(sendPorts[0]);
        ServerSend serverSend1 = new ServerSend(sendPorts[0], cm1);
        serverSend1.start();
        ServerReceive serverRec1 = new ServerReceive(receivePorts[0], cm1);
        serverRec1.start();
        CameraHandler cam1 = new CameraHandler(cm1);
        cam1.start();
        
        CameraMonitor cm2 = new CameraMonitor(sendPorts[1]);
        ServerSend serverSend2 = new ServerSend(sendPorts[1], cm2);
        serverSend2.start();
        ServerReceive serverRec2 = new ServerReceive(receivePorts[1], cm2);
        serverRec2.start();
        CameraHandler cam2 = new CameraHandler(cm2);
        cam2.start();
        
        CameraController.main(new String[]{"localhost", "6077", "6080", "6078", "6081"});
    }
}