package skeleton.client;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JFrame;

public class GuiController extends Thread {
	private int numberOfCameras = 0;
	private boolean showAsynchronous = false;
	private ClientMonitor monitor;
	private Queue<Camera> cameraQueue;
	private GUI gui1;
	private GUI gui2;

	public GuiController() {
		
		monitor = new ClientMonitor(2);
		this.gui1 = new GUI(6077, monitor, 0);
		this.gui2 = new GUI(6080, monitor, 1);
		cameraQueue = new PriorityQueue<Camera>();
		
		new ClientReceive("localhost", 6077, monitor, 0).start();
		new ClientSend("localhost", 6078, monitor, 0).start();

		new ClientReceive("localhost", 6080, monitor, 1).start();
		new ClientSend("localhost", 6081, monitor, 1).start();
	}

	public void run() {
		long relativeTime;
		GUI tempGUI;
		while (true) {
			try {
				relativeTime = monitor.getImage(cameraQueue);
				for (Camera cam : cameraQueue) {
					if (cam.getID() == 0) {
						tempGUI = gui1;

					} else {
						tempGUI = gui2;
					}
					tempGUI.refreshImage(cam.getJpeg(), cam.getTimeStamp() - relativeTime,
							System.currentTimeMillis() - cam.getTimeStamp());
				}
				cameraQueue = new PriorityQueue<Camera>();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
