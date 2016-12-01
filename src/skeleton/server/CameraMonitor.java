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
    private static final byte[] CRLF = "\r\n".getBytes();

    long frameRate = IDLE_FRAMERATE;
    private byte[] imageBox; // The box we keep the latest image in
    private byte[] timeStampBox; // The box we keep the latest timestamp
    private byte[] motionDetectBox;
    private boolean motionDetect = false; // false means idle
    private AxisM3006V cam;
    private boolean connected = false;
    private int count;
    private static int MOTION_DETECTED_DELAY = 10;

    /**
     * Creates camera object and initializes arrays,
     * tries to connect to camera.
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
        System.out.println("Trying to connect to camera");
        if (!cam.connect()) {
            System.out.println("Failed to connect to camera!");
            System.exit(1);
        }
        System.out.println("Connected to camera");
    }

    /**
     * Synchronized method that switches camera mode.
     *
     * @param motionDetect
     * true: activate motion mode
     * false: activate idle mode
     */
    public synchronized void activateMotion(boolean motionDetect) {
        this.motionDetect = motionDetect;
        frameRate = (motionDetect) ? MOTION_FRAMERATE: IDLE_FRAMERATE;
    }

    /**
     * Used by CameraHandler
     *
     * Takes an image with the initialized camera then waits specified
     * time before taking the next.
     */
    public synchronized void takeImage() {
        //cam.getJPEG(imageBox, 0); // put image in imageBox
        //cam.getTime(timeStampBox, 0); // put timestamp in timeStampBox
        //cam.close();
        //notifyAll();
        long timestamp = System.currentTimeMillis();
        while (System.currentTimeMillis() < timestamp + frameRate) {
            cam.getJPEG(imageBox, 0);
            // Motion detected needs an image to detect motion
            if (cam.motionDetected()) activateMotion(true);
            try {
                //System.out.println("Waiting to take picture.");
                wait(MOTION_FRAMERATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cam.getTime(timeStampBox, 0);
        notifyAll();
    }

    /**
     * Synchronized method that sends the last captured image
     * over the network via the provided OutPutStream.
     *
     * The sent data is constructed in the following order:
     * ["IMG " + JPEG + TimeStamp + Mode]
     * @param os
     * OutputStream provided by the network socket.
     * @throws IOException
     */
    public synchronized void sendImage(OutputStream os) throws IOException {
        byte mode = (motionDetect) ? (byte) 1 : (byte) 0;
    	motionDetectBox = new byte[1];
        motionDetectBox[0] = mode;
        System.out.println((int) motionDetectBox[0]);
        System.out.println();
        byte[] imgCmdPacket = new byte[SEND_IMAGE_CMD.length + CRLF.length];
        byte[] imgDataPacket = new byte[imageBox.length + CRLF.length];
        byte[] tsDataPacket = new byte[timeStampBox.length + CRLF.length];
        byte[] motionDetectPacket = new byte[motionDetectBox.length + CRLF.length];
        //System.out.println("Constructed byte arrays");


        // Put "IMG " + CRLF in image command packet
        System.arraycopy(SEND_IMAGE_CMD, 0, imgCmdPacket, 0, SEND_IMAGE_CMD.length);
        System.arraycopy(CRLF, 0, imgCmdPacket, SEND_IMAGE_CMD.length, CRLF.length);
        //System.out.println("copied command");

        // Wait until notified by takeImage
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.arraycopy(imageBox, 0, imgDataPacket, 0, imageBox.length);
        System.arraycopy(CRLF, 0, imgDataPacket, imageBox.length, CRLF.length);
        // System.out.println("copied image data");

        // Put timestamp data and CRLF in timestamp data packet
        System.arraycopy(timeStampBox, 0, tsDataPacket, 0, timeStampBox.length);
        System.arraycopy(CRLF, 0, tsDataPacket, timeStampBox.length, CRLF.length);
        // System.out.println("copied image timestamp");

        //Put motionDetect data and CRLF in motionDetect data packet
        System.arraycopy(motionDetectBox, 0, motionDetectPacket, 0, motionDetectBox.length);
        System.arraycopy(CRLF, 0, motionDetectPacket, motionDetectBox.length, CRLF.length);
        // System.out.println("copied byte for motion detected");

        //Merge data arrays into packet and send
        os.write(imgCmdPacket, 0, imgCmdPacket.length);
        os.write(imgDataPacket, 0, imgDataPacket.length);
        os.write(tsDataPacket, 0, tsDataPacket.length);
        os.write(motionDetectPacket, 0, motionDetectPacket.length);
        //notifyAll();
    }

    /**
     * Tells monitor that a client has connected
     */
    public synchronized void connect() {
        connected = true;
    }

    /**
     * Tells the monitor to disconnect the client
     */
    public synchronized void disconnect() {
    	connected = false;
    }

    /**
     * Returns the state of the current connection
     * @return
     * True: Client is connected
     * False: Client is or is in the process of disconnecting
     */
    public synchronized boolean connected() {
    	return connected;
    }
}
