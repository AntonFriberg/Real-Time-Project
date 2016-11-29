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
        int sendPort = 6032;
        int receivePort = 6033;
        CameraMonitor cm = new CameraMonitor(sendPort);
        ServerSend server = new ServerSend(sendPort, "localhost", cm);
        CameraHandler cam = new CameraHandler(cm);
        cam.start();
        server.start();
        ServerReceive serverRec = new ServerReceive(receivePort,"localhost", cm);
        serverRec.start();
        CameraController.main(new String[]{"localhost", Integer.toString(sendPort),Integer.toString(receivePort)});
    }
}