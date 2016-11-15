package skeleton.server;

/**
 * Created by anton on 11/15/16.
 */
public class ServerSend extends Thread{
    /**
     * Packages the image data, timestamp and current mode into an ImageEvent and sends it
     * over the network via byte array over standardized protocol. There is one send
     * thread for each camera.
     */

    private CameraMonitor cm;

    public ServerSend(CameraMonitor cm) {
        while (true) {

        }
    }
}
