package skeleton.client;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JButton;
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
	private GUI gui1;
	private GUI gui2;
	private JFrame mainFrame;
	private JMenuBar menuBar;
	private JMenu menu, submenu;
	private JMenuItem menuItem;

	public GuiController() {
		mainFrame = new JFrame();
		numberOfCameras = 2;
		monitor = new ClientMonitor(2);

		menuBar = new JMenuBar();
		menu = new JMenu("A Menu");
		menuBar.add(menu);
		menuItem = new JMenuItem("A text-only menu item", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		menu.add(menuItem);
		mainFrame.setJMenuBar(menuBar);
		
		this.gui1 = new GUI(6077, monitor, 0);
		this.gui2 = new GUI(6080, monitor, 1);
		JPanel guiPanel = new JPanel();
		guiPanel.add(gui1);
		guiPanel.add(gui2);
		mainFrame.getContentPane().add(guiPanel);

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
		GUI tempGUI;
		while (true) {

			try {
				if (firstCall) {
					monitor.getAll(cameraQueue);
					relativeTime = cameraQueue.peek().getTimeStamp();
					// mainFrame.pack();

				} else {
					synchronous = monitor.getImage(cameraQueue);
					relativeTime = cameraQueue.peek().getTimeStamp();
				}

				for (Camera cam : cameraQueue) {
					System.out.println("Sleep for" + (cam.getTimeStamp() - relativeTime));
					Thread.sleep(cam.getTimeStamp() - relativeTime);
					if (cam.getID() == 0) {
						tempGUI = gui1;

					} else {
						tempGUI = gui2;
					}
					tempGUI.refreshImage(cam.getJpeg(), cam.getTimeStamp() - relativeTime,
							System.currentTimeMillis() - cam.getTimeStamp(), cam.motionDetect());
					relativeTime = cam.getTimeStamp();
				}
				if (firstCall) {
					mainFrame.setVisible(true);
					mainFrame.pack();
					firstCall = false;
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
