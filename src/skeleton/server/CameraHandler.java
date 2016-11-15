package skeleton.server;

/**
 * Created by anton on 11/15/16.
 */
public class CameraHandler extends Thread{
    /**
     * Periodic thread that takes images according to set settings. The images,
     * timestamp and current settings are carried out inside the monitor in
     * order to maintain thread-safe interaction.
     */

    private CameraMonitor cm;
    private int period = 1000; // wake thread every second

    public CameraHandler(CameraMonitor cm) {
        this.cm = cm;
    }

    public void run() {
        while(true) {
            try {
                sleep(period);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //cm.takeImage();
        }
    }
}
