package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientMonitor {
	private byte[] imgBuffer;
	private byte[] timeStampBuffer;
	private byte[] motionDetectBuffer;

	public static final int IDLE_MODE = 0;
	public static final int MOVIE_MODE = 1;
	public static final int DISCONNECT = 2;
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
	 * Fetches an image if there is one to fetch.
	 * 
	 * @param image
	 * @throws InterruptedException
	 */
	public synchronized void getImage(byte[] image, byte[] timeStamp, byte[] motionDetect) throws InterruptedException {
		while (!hasImage)
			wait();
		System.arraycopy(imgBuffer, 0, image, 0, imgBuffer.length);
		System.arraycopy(timeStampBuffer, 0, timeStamp, 0, timeStampBuffer.length);
		motionDetect[0] = motionDetectBuffer[0];
		System.out.println("Fetch");
		hasImage = false;
		notifyAll();
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
	 * @return
	 */
	public synchronized boolean shouldDisconnect() {
		return receiveShouldDisconnect;
	}

}
