package skeleton.client;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientMonitor {

	public static final int MOTION_OFF = 0;
	public static final int MOTION_ON = 1;
	public static final int DISCONNECT = 2;
	public static final int CONNECT = 3;
	public static final int AUTO_MODE = 4;
	public static final int MANUAL_MODE = 5;
	public static final byte[] CRLF = { 13, 10 };
	public static final int REC_DATA = AxisM3006V.IMAGE_BUFFER_SIZE + AxisM3006V.TIME_ARRAY_SIZE + CRLF.length * 3 + 1;
	public static final int SYNCHRONIZATION_THRESHOLD = 200; // 200 milliseconds
	private static final int ASYNCHRONOUS_IMAGES_THRESHOLD = 10;
	private Queue<Camera> cameraQueue; // A temporary storage for cameras which
										// are to be displayed
	private HashMap<Integer, Boolean> cmdMap; // Stores which cameras that have
												// sent the commands

	private int numberOfCameras;
	private boolean autoMode = true;
	private boolean showSynchronous = true;
	private int command;
	private int prevMotion = MOTION_OFF; // In order to notify when motion is
											// detected in one camera the first
											// time this variable is used
	private boolean receiveShouldDisconnect = false;
	private int motionTriggerID; // The camer which triggered the motion
									// activate
	private int outOfSynchCounter = 0;

	public ClientMonitor(int numberOfCameras) {
		cmdMap = new HashMap<Integer, Boolean>();
		for (int i = 0; i < numberOfCameras; i++) {
			cmdMap.put(i, false);
		}
		this.numberOfCameras = numberOfCameras;
		cameraQueue = new PriorityQueue<Camera>(numberOfCameras, new Comparator<Camera>() {
			@Override
			public int compare(Camera c1, Camera c2) {
				return (int) (c1.getTimeStamp() - c2.getTimeStamp());
			}
		});
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
		if (!isInQueue(cameraID)) { // Should only post one image per gui
									// refresh cycle
			Camera cam = new Camera(cameraID); // Temporary Camerastorage
			System.arraycopy(image, 0, cam.getJpeg(), 0, image.length);
			cam.setTimeStamp(convertTime(timeStamp));
			if (motionDetect == MOTION_ON) {
				cam.setMotionDetect(true);
				if (autoMode && prevMotion == MOTION_OFF) {
					setCommand(MOTION_ON); // Send motion to all cameras
					motionTriggerID = cameraID;
				}
			} else {
				cam.setMotionDetect(false);
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

	/**
	 * 
	 * @param fetchQueue
	 * @throws InterruptedException
	 */
	public synchronized void getImage(Queue<Camera> fetchQueue) throws InterruptedException {
		while (cameraQueue.size() == 0) {
			wait();
		}
		// An image is received
		long receivedTime = System.currentTimeMillis(); // Time when image was
														// received
		while ((System.currentTimeMillis() - receivedTime < SYNCHRONIZATION_THRESHOLD)
				&& cameraQueue.size() < numberOfCameras) {
			wait(100);
		}

		if (cameraQueue.size() == numberOfCameras) {
			outOfSynchCounter = 0;
		} else {
			outOfSynchCounter++;
		}
		if (outOfSynchCounter == ASYNCHRONOUS_IMAGES_THRESHOLD) { // One frame
																	// out of
																	// synch is
																	// allowed
																	// but if
																	// there are
																	// more than
																	// ASYNCHRONOUS_IMAGES_THRESHOLD
																	// images in
																	// succession
																	// then
																	// display
																	// asynchronous
			showSynchronous = false;
			outOfSynchCounter = 0;
		}

		while (!this.cameraQueue.isEmpty()) {
			fetchQueue.add(cameraQueue.poll());
		}
		System.out.println("Fetch");
	}

	/**
	 * 
	 * @param fetchQueue
	 * @throws InterruptedException
	 */
	public synchronized void getAll(Queue<Camera> fetchQueue) throws InterruptedException {
		while (cameraQueue.size() < numberOfCameras) {
			wait();
		}
		while (!this.cameraQueue.isEmpty()) {
			fetchQueue.add(cameraQueue.poll());
		}
		System.out.println("Fetched All");
	}

	/**
	 * changes the mode of synchronous displaying
	 * 
	 */
	public synchronized void changeSynchronousMode() {
		showSynchronous = !showSynchronous;
	}

	/**
	 * Tells a displayer of images whether the images should be displayed
	 * synchronously
	 * 
	 * @return
	 */
	public synchronized boolean displaySynchronous() {
		return showSynchronous == true;
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
	public synchronized int getCommand(int cameraID) throws InterruptedException {
		while (!camHasCommand(cameraID))
			wait();
		notifyAll();
		return command;
	}

	private void setCommandAvailable() {
		for (Entry<Integer, Boolean> entry : cmdMap.entrySet()) {
			entry.setValue(true);
		}
	}

	private boolean camHasCommand(int cameraID) {
		if (cmdMap.containsKey(cameraID)) {
			return cmdMap.replace(cameraID, false);
		}
		return false;
	}

	/**
	 * Sets a new command and notifies the ClientSend that there is a new
	 * command
	 * 
	 * @param newCommand
	 *            the command that is to be changed
	 * @throws InterruptedException
	 */
	public synchronized void setCommand(int newCommand) throws InterruptedException {
		switch (newCommand) {
		case DISCONNECT:
			setDisconnect();
			break;
		case CONNECT:
			setConnect();
			break;
		case MOTION_OFF:
			prevMotion = MOTION_OFF;
			break;
		case MOTION_ON:
			prevMotion = MOTION_ON;
			break;
		case AUTO_MODE:
			autoMode = true;
			break;
		case MANUAL_MODE:
			autoMode = false;
			break;
		}
		setCommandAvailable();
		command = newCommand;
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
	 * @return if client should disconnect
	 */
	public synchronized boolean shouldDisconnect() {
		return receiveShouldDisconnect;
	}

	/**
	 * The disconnected sending threads wait here for new orders
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void waitForConnect() throws InterruptedException {
		while (receiveShouldDisconnect)
			wait();
	}

	public synchronized int getTriggerID() {
		int tempID = motionTriggerID;
		motionTriggerID = -1;
		return tempID;
	}
}
