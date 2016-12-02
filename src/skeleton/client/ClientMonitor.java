package skeleton.client;

import java.util.ArrayList;
import java.util.Comparator;
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
	
	public static final int CONNECT = 3;
	public static final int AUTO_MODE = 4;
	public static final int MANUAL_MODE = 5;
	
	public static final byte[] CRLF = { 13, 10 };
	public static final int REC_DATA = AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE + CRLF.length * 3 + 1;
	public static final int SYNCHRONIZATION_THRESHOLD = 20; // 200 milliseconds

	private int numberOfCameras;
	private boolean receiveShouldDisconnect;

	public ClientMonitor(int numberOfCameras) {
		this.numberOfCameras = numberOfCameras;
		cameraQueue = new PriorityQueue<Camera>(numberOfCameras, new Comparator<Camera>() {
			@Override
			public int compare(Camera c1, Camera c2) {
				return (int) (c1.getTimeStamp() - c2.getTimeStamp());
			}
		});
		commandMap = new HashMap<Integer, Integer>();
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
		if (!isInQueue(cameraID)) {
			Camera cam = new Camera(cameraID);
			System.arraycopy(image, 0, cam.getJpeg(), 0, image.length);
			cam.setTimeStamp(convertTime(timeStamp));
			if (motionDetect == IDLE_MODE) {
				cam.setMotionDetect(false);
			} else {
				cam.setMotionDetect(true);
			}
			cameraQueue.offer(cam);
			System.out.println("Put");
			notifyAll();
		} else {
			System.out.println("Already in queue");
		}
	}

	private boolean isInQueue(int cameraID) {
		for (Camera cam : cameraQueue) {
			if (cam.getID() == cameraID)
				return true;
		}
		return false;
	}

	public synchronized void getAll(Queue<Camera> fetchQueue) throws InterruptedException{
		while (cameraQueue.size() < numberOfCameras) {
			wait();
		}
		while (!this.cameraQueue.isEmpty()) {
			fetchQueue.add(cameraQueue.poll());
		}
		System.out.println("Fetched All");
	}
	/**
	 * 
	 * @param fetchQueue
	 * @throws InterruptedException
	 */
	public synchronized boolean getImage(Queue<Camera> fetchQueue) throws InterruptedException {
		while (cameraQueue.size() == 0) {
			wait();
		}
		boolean synchronous = false;
		long receivedTime = System.currentTimeMillis();
		while ((System.currentTimeMillis() - receivedTime < SYNCHRONIZATION_THRESHOLD)
				&& cameraQueue.size() < numberOfCameras) {
			wait(100);
		}
		if (cameraQueue.size() == numberOfCameras) {
			synchronous = true;
		}
		while (!this.cameraQueue.isEmpty()) {
			fetchQueue.add(cameraQueue.poll());
		}
		System.out.println("Fetch");
		return synchronous;
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
		if (newCommand == ClientMonitor.DISCONNECT) {
			setDisconnect();
		} else if(newCommand == ClientMonitor.CONNECT) {
			setConnect();
		}
		commandMap.put(callerID, newCommand);
		notifyAll();

	}

	private void setDisconnect() {
		receiveShouldDisconnect = true;
	}
	
	private void setConnect() {
		receiveShouldDisconnect = false;
	}
	/**
	 * Notifies the recieving thread that it should cancel receiving images
	 * 
	 * @return
	 */
	public synchronized boolean shouldDisconnect() {
		return receiveShouldDisconnect;
	}
	
	public synchronized void shouldConnect() throws InterruptedException{
		while(!receiveShouldDisconnect)
			wait();
	}
	

}
