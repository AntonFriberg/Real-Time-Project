package skeleton.server;

/**
 * Created by Anton Friberg and Joakim Magnusson on 11/15/16.
 */
public class CameraMonitor {
    /**
     * Keeps hold of the latest produced images, timestamps and current modes. Contains
     * thread safe methods for each of the necessary interactions on the contained data.
     */

    private byte[] imageBox; // The box we keep the latest image in
    private boolean motionDetect = false; // false means idle
    private String time_stamp;
    private boolean hasImage;


    public CameraMonitor() {
        // Constructor
    }

    /**
    public synchronized void sendImage(byte[] image) throws InterruptedException {
        while(hasImage) wait();
        System.arraycopy(image, 0, imageBox, image.length);
        hasImage = true;
        notifyAll();
    }

    public synchronized void getImage(byte[] image) throws InterruptedException {
        while(!hasImage) wait();
        System.arraycopy(imageBox, 0, image, image.length);
        hasImage = false;
        notifyAll();
    }

    synchronized void takeImage() {
        // Method for taking image.
    }
    */

    /**
     * Synchronized method that switches camera mode.
     * @param motionDetect
     * true: activate motion mode
     * false: activate idle mode
     */
    public synchronized void activateMotion(boolean motionDetect) {
        this.motionDetect = motionDetect;
    }
}
