package skeleton.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class GuiController extends Thread {

	private int numberOfCameras = 0;
	private boolean showAsynchronous = false;
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
		boolean synchronous = true;
		boolean firstCall = true;
		long relativeTime;
		while (true) {

			try {
				if (firstCall) {
					monitor.getAll(cameraQueue);
					relativeTime = cameraQueue.peek().getTimeStamp();

				} else {
					synchronous = monitor.getImage(cameraQueue);
					relativeTime = cameraQueue.peek().getTimeStamp();
				}
				
				
				for (Camera cam : cameraQueue) {

					if (!showAsynchronous) {
						System.out.println("Sleep for" + (cam.getTimeStamp() - relativeTime));
						Thread.sleep(cam.getTimeStamp() - relativeTime);
					}
					
					gui.refreshImage(cam.getJpeg(),
							System.currentTimeMillis() - cam.getTimeStamp(), cam.getID());
					relativeTime = cam.getTimeStamp();
					
					if(firstCall){
						gui.firstCallInitiate();
						firstCall = false;

					}
				}
				
				while (!cameraQueue.isEmpty()) {
					cameraQueue.poll();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
