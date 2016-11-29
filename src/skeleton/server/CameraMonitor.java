package skeleton.server;

import se.lth.cs.eda040.fakecamera.AxisM3006V;
//import se.lth.cs.eda040.proxycamera.AxisM3006V;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Anton Friberg and Joakim Magnusson on 11/15/16.
 */
public class CameraMonitor {
    /**
     * Keeps hold of the latest produced images, timestamps and current modes. Contains
     * thread safe methods for each of the necessary interactions on the contained data.
     */
    private static long IDLE_FRAMERATE = 5000;
    private static long MOTION_FRAMERATE = 40;
    private static final byte[] SEND_IMAGE_CMD = "IMG ".getBytes();
    private static final byte[] EOL = "\r\n".getBytes();
    long frameRate = MOTION_FRAMERATE;
    private byte[] imageBox; // The box we keep the latest image in
    private byte[] timeStampBox; // The box we keep the latest timestamp
    private byte[] motionDetectBox;
    private boolean motionDetect = false; // false means idle
    private boolean hasImage;
    private AxisM3006V cam;
    private boolean connected = false;

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
        timeStampBox = new byte[AxisM3006V.TIME_ARRAY_SIZE];
        if (!cam.connect()) {
            System.out.println("Failed to connect to camera!");
            System.exit(1);
        }
        takeImage();
    }

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
        long currentTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < currentTime + frameRate) {
            try {
                //System.out.println("Waiting to take picture.");
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cam.getJPEG(imageBox, 0); // put image in imageBox
        cam.getTime(timeStampBox, 0); // put timestamp in timeStampBox
        //cam.close();
        notifyAll();
    }

    public synchronized void sendImage(OutputStream os) throws IOException {
    	motionDetectBox = (motionDetect) ? new byte[(byte) 1] : new byte[(byte) 0];
        byte[] imgCmdPacket = new byte[SEND_IMAGE_CMD.length + EOL.length];
        byte[] imgDataPacket = new byte[imageBox.length + EOL.length];
        byte[] tsDataPacket = new byte[timeStampBox.length + EOL.length];
        byte[] motionDetectPacket = new byte[motionDetectBox.length + EOL.length];
        byte[] packet = new byte[imgCmdPacket.length + imgDataPacket.length + tsDataPacket.length];
        System.out.println("Constructed byte arrays");

        /**
         * Put "IMG " + EOL in image command packet
         */
        System.arraycopy(SEND_IMAGE_CMD, 0, imgCmdPacket, 0, SEND_IMAGE_CMD.length);
        System.arraycopy(EOL, 0, imgCmdPacket, SEND_IMAGE_CMD.length, EOL.length);
        System.out.println("copied command");
        /**
         * Put image data and EOL in image data packet
         */
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.arraycopy(imageBox, 0, imgDataPacket, 0, imageBox.length);
        System.arraycopy(EOL, 0, imgDataPacket, imageBox.length, EOL.length);
        System.out.println("copied image data");
        /**
         * Put timestamp data and EOL in timestamp data packet
         */
        System.arraycopy(timeStampBox, 0, tsDataPacket, 0, timeStampBox.length);
        System.arraycopy(EOL, 0, tsDataPacket, timeStampBox.length, EOL.length);
        System.out.println("copied image timestamp");
        /**
         * Put motionDetect data and EOL in motionDetect data packet
         */
        System.arraycopy(motionDetect, 0, motionDetectPacket, 0, motionDetectBox.length);
        System.arraycopy(EOL, 0, motionDetectPacket, motionDetectBox.length, EOL.length);
        System.out.println("copied byte for motion detected");
        /**
         * Merge data arrays into packet and send
         */

        os.write(imgCmdPacket, 0, imgCmdPacket.length);
        os.write(imgDataPacket, 0, imgDataPacket.length);
        os.write(tsDataPacket, 0, tsDataPacket.length);
        os.write(motionDetectPacket, 0, motionDetectPacket.length);
        notifyAll();
    }

    public synchronized void connect() {
        connected = true;
    }
    
    public synchronized void disconnect() {
    	connected = false;
    }
    
    public synchronized boolean connected() {
    	return connected;
    }
    public synchronized boolean motionDetected(){
    	motionDetect = cam.motionDetected();
    	return motionDetect;
    }
}
