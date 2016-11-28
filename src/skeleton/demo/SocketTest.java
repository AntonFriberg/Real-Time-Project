package skeleton.demo;

import skeleton.client.CameraController;
import skeleton.client.ClientMonitor;
import skeleton.client.ClientReceive;
import skeleton.server.CameraMonitor;
import skeleton.server.ServerReceive;
import skeleton.server.ServerSend;

public class SocketTest {

    public static void main(String[] args) {
        CameraMonitor cam = new CameraMonitor(6077);
        ServerSend server = new ServerSend(6077, "localhost", cam);
        server.start();
        ServerReceive serverRec = new ServerReceive(6078,"localhost",cam);
        serverRec.start();
        CameraController.main(new String[]{"localhost","6077","6078"});
    }
}