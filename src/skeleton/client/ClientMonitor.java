package skeleton.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientMonitor {
	// private byte[] imgBuffer;
	// private byte[] timeStampBuffer;
	// private byte[] motionDetectBuffer;
	//
	private Queue<Camera> cameraQueue;
	private HashMap<Integer, Integer> commandMap;
	public static final int IDLE_MODE = 0;
	public static final int MOVIE_MODE = 1;
	public static final int DISCONNECT = 2;
	public static final byte[] CRLF = { 13, 10 };
	public static final int REC_DATA = AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE + CRLF.length * 3 + 1;
	public static final int SYNCHRONIZATION_THRESHOLD = 20; // 200 milliseconds

	private long refTime = Long.MAX_VALUE;
	private int numberOfCameras;
	private boolean receiveShouldDisconnect;

	public ClientMonitor(int numberOfCameras) {
		this.numberOfCameras = numberOfCameras;
		cameraQueue = new PriorityQueue<Camera>();
		commandMap = new HashMap<Integer, Integer>();
	}

	private void setRefTime(long timeStamp) {
		if (timeStamp < refTime) {
			refTime = timeStamp;
		}
	}

	/**
	 * Stores a byte array containing an image in a buffer. Does not care
	 * whether the last image has been displayed or not.
	 * 
	 * @param image,
	 *            an array of bytes containing the image
	 * @throws InterruptedException
	 */
	public synchronized void putImage(byte[] image, byte[] timeStamp, byte motionDetect, int cameraID)
			throws InterruptedException {
		cameraQueue.offer(new Camera(cameraID));
		System.arraycopy(image, 0, cameraQueue.peek().getJpeg(), 0, image.length);
		cameraQueue.peek().setTimeStamp(convertTime(timeStamp));
		if (motionDetect == IDLE_MODE) {
			cameraQueue.peek().setMotionDetect(false);
		} else {
			cameraQueue.peek().setMotionDetect(true);
		}
		setRefTime(convertTime(timeStamp));
		System.out.println("Put");
		notifyAll();
	}

	/**
	 * 
	 * @param fetchQueue
	 * @throws InterruptedException
	 */
	public synchronized long getImage(Queue<Camera> fetchQueue) throws InterruptedException {
		while (cameraQueue.size() == 0) {
			wait();
		}
		long receivedTime = System.currentTimeMillis();
		while ((System.currentTimeMillis() - receivedTime < SYNCHRONIZATION_THRESHOLD)
				&& cameraQueue.size() < numberOfCameras) {
			wait(100);
		}

		while (!this.cameraQueue.isEmpty()) {
			fetchQueue.add(cameraQueue.poll());
		}
		long timePlaceHolder = refTime;
		refTime = Long.MAX_VALUE;
		System.out.println("Fetch");
		return timePlaceHolder;
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
	public synchronized int getCommand(int callerID) throws InterruptedException {
		while (!commandMap.containsKey(callerID))
			wait();
		notifyAll();
		return commandMap.remove(callerID);
	}

	/**
	 * Sets a new command and notifies the ClientSend that there is a new
	 * command
	 * 
	 * @param newCommand
	 *            the command that is to be changed
	 * @throws InterruptedException
	 */
	public synchronized void setCommand(int newCommand, int callerID) throws InterruptedException {
		while (commandMap.containsKey(callerID))
			wait();
		if (newCommand == ClientMonitor.DISCONNECT){
			setDisconnect();
		}
		commandMap.put(callerID, newCommand);
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
