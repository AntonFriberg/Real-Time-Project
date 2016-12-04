package skeleton.server;

/**
 * Created by Anton Friberg and Joakim Magnusson on 11/15/16.
 * Thread that takes images
 */
public class CameraHandler extends Thread{
    /**
     * Periodic thread that takes images according to set settings. The images,
     * timestamp and current settings are carried out inside the monitor in
     * order to maintain thread-safe interaction.
     */
    private CameraMonitor cm;

    public CameraHandler(CameraMonitor cm) {
        this.cm = cm;
    }

    public void run() {
        while(true) {
            while(!cm.connected()) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cm.takeImage();
        }
    }
}
