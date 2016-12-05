package skeleton.client;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class GuiController extends Thread {

	private int numberOfCameras = 0;
	private ClientMonitor monitor;
	private Queue<Camera> cameraQueue;
	private GUI gui;

	public GuiController(ArrayList<String> recPorts, ArrayList<String> sendPorts) {
		numberOfCameras = recPorts.size();
		for (int i = 0; i < numberOfCameras; i++) {
			try {
				int recPort = Integer.parseInt(recPorts.get(i));
				int sendPort = Integer.parseInt(sendPorts.get(i));
				new ClientReceive("localhost", recPort, monitor, 0).start();
				new ClientSend("localhost", sendPort, monitor, 0).start();
			} catch (NumberFormatException e) {
				numberOfCameras--; //This is not a port
			}
		}
		
		monitor = new ClientMonitor(numberOfCameras);
		this.gui = new GUI(monitor, numberOfCameras);
		cameraQueue = new PriorityQueue<Camera>(numberOfCameras, new Comparator<Camera>() {
			@Override
			public int compare(Camera c1, Camera c2) {
				return (int) (c1.getTimeStamp() - c2.getTimeStamp());
			}
		});
	}

	public void run() {
		boolean synchronous = true;
		boolean firstCall = true;
		long relativeTime;
		boolean anyHasMotion = false;

		while (true) {
			try {
				if (firstCall) { // Wait for all cameras to send images before
									// first display
					monitor.setCommand(ClientMonitor.AUTO_MODE);
					monitor.getAll(cameraQueue);
					relativeTime = cameraQueue.peek().getTimeStamp();

				} else {
					monitor.getImage(cameraQueue);
					synchronous = monitor.displaySynchronous();
					relativeTime = cameraQueue.peek().getTimeStamp();
				}

				// Display the image from the cameras
				for (Camera cam : cameraQueue) {

					if (synchronous) { // Show synchronous
						System.out.println("Sleep for" + (cam.getTimeStamp() - relativeTime));
						Thread.sleep(cam.getTimeStamp() - relativeTime);
					}
					if (!anyHasMotion && cam.motionDetect()) {
						anyHasMotion = true;
					}
					gui.refreshImage(cam.getJpeg(), System.currentTimeMillis() - cam.getTimeStamp(), cam.getID());
					relativeTime = cam.getTimeStamp();

					if (firstCall) { // Initiate window
						gui.firstCallInitiate();

						firstCall = false;
					}
				}

				// Display the current mode of display
				gui.setMode(anyHasMotion);

				// Display the current mode of synchronous
				gui.setSynchIndicator(synchronous);

				gui.displayMotionTriggerID(monitor.getTriggerID());

				anyHasMotion = false;

				while (!cameraQueue.isEmpty()) {
					cameraQueue.poll();
				}
			} catch (Exception e) {
				System.out.println("Something went wrong");
				break;
			}
		}
	}
}
