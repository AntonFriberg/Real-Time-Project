package skeleton.demo;

import skeleton.client.ClientMonitor;
import skeleton.client.ClientReceive;
import skeleton.server.CameraMonitor;
import skeleton.server.ServerSend;

public class SocketTest {

    public static void main(String[] args) {
        CameraMonitor cam = new CameraMonitor(6077);
        ClientMonitor cm = new ClientMonitor();
        ServerSend server = new ServerSend(6077, "localhost", cam);
        ClientReceive client = new ClientReceive("localhost", 6077, cm);

        server.start();
        client.start();
    }
}