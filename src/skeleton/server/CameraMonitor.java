package skeleton.server;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

/**
 * Created by Anton Friberg and Joakim Magnusson on 11/15/16.
 */
public class CameraMonitor {
    /**
     * Keeps hold of the latest produced images, timestamps and current modes. Contains
     * thread safe methods for each of the necessary interactions on the contained data.
     */
    private static int IDLE_FRAMERATE = 5000;
    private static int MOTION_FRAMERATE = 40;
    private int frameRate = IDLE_FRAMERATE;
    private byte[] imageBox; // The box we keep the latest image in
    private byte[] timeStampBox; // The box we keep the latest timestamp
    private boolean motionDetect = false; // false means idle
    private boolean hasImage;
    private AxisM3006V cam;

    /**
     *
     * @param port
     * camera port
     */
    public CameraMonitor(int port) {
        cam = new AxisM3006V();
        cam.init();
        cam.setProxy("argus-1.student.lth.se", port);
        imageBox = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
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
        frameRate = (motionDetect) ? MOTION_FRAMERATE: IDLE_FRAMERATE;
    }

    public synchronized void takeImage() {
        try {
            wait(frameRate);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!cam.connect()) {
            System.out.println("Failed to connect to camera!");
            System.exit(1);
        }
        cam.getTime(timeStampBox, 0); // put timestamp in timeStampBox
        cam.getJPEG(imageBox, 0); // put image in imageBox
        cam.close();
    }
}
