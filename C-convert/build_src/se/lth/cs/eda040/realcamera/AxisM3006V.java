package se.lth.cs.eda040.realcamera;


/**
 * This class encapsulates a hardware interface in c to Axis M3006V camera series. It can connect
 * (open up a stream) to the underlying hardware interface and then capture JPEG images from that stream
 * at 25 fps and with a resolution of 640x480. It is not thread safe.
 */
public class AxisM3006V{
    public static final int TIME_ARRAY_SIZE  = 8;
    public static final int IMAGE_BUFFER_SIZE = 131072;
    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 480;

    private boolean motion;

    private native boolean nativeConnect();
    private native int nativeGetJPEG(byte[] target, int offset);
    private native void nativeClose();
    private native void nativeGetTime(byte[] target, int offset);

    // Methods not used
    public void init() {}
    public void destroy() {}
    public void setProxy(String host, int port) {}

    /**
     * Connects to the camera.
     *
     * @return true if connected otherwise false.
     */
    public boolean connect(){
	return nativeConnect();
    }

    /**
     * Reads an image from the camera and puts it in the array target starting at index offset
     * The size of target needs to be at least offset+IMAGE_BUFFER_SIZE
     * @param target Byte array to put data in.
     * @param offset Offset from the start of the byte array.
     *
     * @return The length of the image captured, 0 if no picture was captured 
     */
    public int getJPEG(byte[] target, int offset) {
	int result = nativeGetJPEG(target, offset);
	int value_pos = 68+offset;
	motion = target[value_pos] == '1';
	return result;
    }

    /**
     * Puts the capture time of the latest image in the specified target byte array, starting at
     * offset. The resolution is milliseconds.
     * 
     * @param target
     *            the array to be written into
     * @param offset
     *            the starting position
     */
    public void getTime(byte[] target, int offset){
	int minLength = TIME_ARRAY_SIZE + offset;
	if (target.length < minLength) {
	    throw new IllegalArgumentException("Length of target is too short, is " + target.length +" should be atleast" + minLength);
	}
	nativeGetTime(target,offset);
    }

    /**
     * Returns whether or not motion was detected in the latest image. This is taken from the cameras
     * built in motion detection and is extracted from the JPEG header whenever a new JPEG is fetched 
     * from the camera.
     */
    public boolean motionDetected() {
	return motion;
    }

    /**
     * Closes the camera connection.
     */
    public void close() {
	nativeClose();
    }
}
