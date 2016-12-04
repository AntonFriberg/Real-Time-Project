package skeleton.client;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class GuiController extends Thread {

	private int numberOfCameras = 0;
	private ClientMonitor monitor;
	private Queue<Camera> cameraQueue;
	private GUI gui;

	public GuiController() {
		numberOfCameras = 2;
		monitor = new ClientMonitor(2);

		this.gui = new GUI(monitor, 2);
		cameraQueue = new PriorityQueue<Camera>(numberOfCameras, new Comparator<Camera>() {
			@Override
			public int compare(Camera c1, Camera c2) {
				return (int) (c1.getTimeStamp() - c2.getTimeStamp());
			}
		});

		new ClientReceive("localhost", 6077, monitor, 0).start();
		new ClientSend("localhost", 6078, monitor, 0).start();

		new ClientReceive("localhost", 6080, monitor, 1).start();
		new ClientSend("localhost", 6081, monitor, 1).start();
	}

	public void run() {
		boolean asynchronous = true;
		boolean firstCall = true;
		long relativeTime;
		boolean anyHasMotion = false;

		while (true) {
			try {
				if (firstCall) {
					monitor.getAll(cameraQueue);
					relativeTime = cameraQueue.peek().getTimeStamp();

				} else {
					monitor.getImage(cameraQueue);
					asynchronous = monitor.showAsynchronous();
					relativeTime = cameraQueue.peek().getTimeStamp();
				}

				for (Camera cam : cameraQueue) {

					if (!asynchronous) {
						System.out.println("Sleep for" + (cam.getTimeStamp() - relativeTime));
						Thread.sleep(cam.getTimeStamp() - relativeTime);
					}
					if (!anyHasMotion && cam.motionDetect()) {
						anyHasMotion = true;
					}
					gui.refreshImage(cam.getJpeg(), System.currentTimeMillis() - cam.getTimeStamp(), cam.getID());
					relativeTime = cam.getTimeStamp();

					if (firstCall) {
						gui.firstCallInitiate();
						firstCall = false;
					}
				}
				gui.setMode(anyHasMotion);
				gui.setSynchIndicator(!asynchronous);
				anyHasMotion = false;

				while (!cameraQueue.isEmpty()) {
					cameraQueue.poll();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
