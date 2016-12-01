package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientMonitor {
	private byte[] imgBuffer;
	private byte[] timeStampBuffer;
	private byte[] motionDetectBuffer;

	public static final int IDLE_MODE = 0;
	public static final int MOVIE_MODE = 1;
	public static final int DISCONNECT = 2;
	public static final int CONNECT = 3;
	public static final int AUTO_MODE = 4;
	public static final byte[] CRLF = { 13, 10 };
	public static final int REC_DATA = AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE + CRLF.length * 3 + 1;
	public static final int SYNCHRONIZATION_THRESHOLD = 20; // 200 milliseconds
	private int currentMode = 0;
	private boolean modeChanged;
	private boolean hasImage;
	private boolean receiveShouldDisconnect;

	public ClientMonitor() {
		hasImage = false;
		modeChanged = false;
		imgBuffer = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
		timeStampBuffer = new byte[AxisM3006V.TIME_ARRAY_SIZE];
		motionDetectBuffer = new byte[1];
	}

	/**
	 * Stores a byte array containing an image in a buffer. Does not care
	 * whether the last image has been displayed or not.
	 * 
	 * @param image,
	 *            an array of bytes containing the image
	 * @throws InterruptedException
	 */
	public synchronized void putImage(byte[] image, byte[] timeStamp, byte motionDetect) throws InterruptedException {
		System.arraycopy(image, 0, imgBuffer, 0, image.length);
		System.arraycopy(timeStamp, 0, timeStampBuffer, 0, timeStamp.length);
		motionDetectBuffer[0] = motionDetect;
		System.out.println("Put");
		hasImage = true;
		notifyAll();
	}

	/**
	 * Fetches an image if one arrives in the designated interval according to
	 * the synchronization threshold.
	 * 
	 * @param camera
	 *            the placeholder of image data
	 * @return true if there was an image at this specific time
	 * @throws InterruptedException
	 */
	public synchronized boolean getImage(Camera camera) throws InterruptedException {
		if (!hasImage) {
			wait(SYNCHRONIZATION_THRESHOLD);
		}
		if (!hasImage) {
			return false;
		}
		System.arraycopy(imgBuffer, 0, camera.getJpeg(), 0, imgBuffer.length);
		camera.setTimeStamp(convertTime(timeStampBuffer));
		if (motionDetectBuffer[0] == IDLE_MODE) {
			camera.setMotionDetect(false);
		} else {
			camera.setMotionDetect(true);
		}
		System.out.println("Fetch");
		hasImage = false;
		notifyAll();
		return true;
	}

	/**
	 * Checks if an image is in the placeholder variables, in that case it
	 * collects the image and stores in the sent in camera variable
	 * 
	 * @param camera
	 *            the placeholder of image data
	 * @return true if there was an image at this specific time
	 */
	public synchronized boolean tryGetImage(Camera camera) {
		if (!hasImage) {
			return false;
		}
		System.arraycopy(imgBuffer, 0, camera.getJpeg(), 0, imgBuffer.length);
		camera.setTimeStamp(convertTime(timeStampBuffer));
		if (motionDetectBuffer[0] == IDLE_MODE) {
			camera.setMotionDetect(false);
		} else {
			camera.setMotionDetect(true);
		}
		System.out.println("Fetch");
		hasImage = false;
		notifyAll();
		return true;
	}

	private long convertTime(byte[] timeArray) {
		long time = 0;
		for (int i = 0; i < timeArray.length; i++) {
			time += ((long) timeArray[i] & 0xffL) << (8 * (7 - i));
		}
		return time;
	}

	/**
	 * Communication between the cameraInterface and the ClientSend in order to
	 * change from movie to idle The ClientSend waits here for the next command.
	 * 
	 * @return the currentMode of operation
	 * @throws InterruptedException
	 */
	public synchronized int getCommand() throws InterruptedException {
		while (!modeChanged)
			wait();
		modeChanged = false;
		return currentMode;
	}

	/**
	 * Sets a new command and notifies the ClientSend that there is a new
	 * command
	 * 
	 * @param newCommand
	 *            the command that is to be changed
	 */
	public synchronized void setCommand(int newCommand) {
		if (newCommand == ClientMonitor.DISCONNECT) {
			setDisconnect();
		}
		currentMode = newCommand;
		modeChanged = true;
		notifyAll();
	}

	private void setDisconnect() {
		receiveShouldDisconnect = true;
	}

	/**
	 * Notifies the recieving thread that it should cancel receiving images
	 * 
	 * @return
	 */
	public synchronized boolean shouldDisconnect() {
		return receiveShouldDisconnect;
	}

}
