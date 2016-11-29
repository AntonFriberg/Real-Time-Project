package skeleton.client;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class CameraInterface extends Thread {
	private GUI gui;
	private ClientMonitor monitor;
	private byte[] jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
	private byte[] timeStamp = new byte[AxisM3006V.TIME_ARRAY_SIZE];
	private byte[] motionDetectStatus = new byte[1];
	private String server, receivePort, sendPort;
	private long initialTimeDifference = -1;

	public CameraInterface(String server, String receivePort, String sendPort) {
		monitor = new ClientMonitor(); // The monitor between
		gui = new GUI(server, Integer.parseInt(receivePort), monitor);
		this.server = server;
		this.receivePort = receivePort;
		this.sendPort = sendPort;
	}

	public void run() {
		ClientReceive clientReceive = new ClientReceive(server, Integer.parseInt(receivePort), monitor);
		clientReceive.start();
		ClientSend clientSend = new ClientSend(server, Integer.parseInt(sendPort), monitor);
		clientSend.start();
		while (true) {
			try {
				monitor.getImage(jpeg, timeStamp, motionDetectStatus);
				gui.refreshImage(jpeg, getDelay(timeStamp));
				gui.setMode(motionDetectStatus[0]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private long getDelay(byte[] timeArray) {
		convertTime(timeArray);
		return System.currentTimeMillis();
	}

	public static void convertTime(byte[] timeArray) {
		 long stime = System.currentTimeMillis();
		 int index = 0;
		 timeArray[index++] = (byte) ((stime & 0xff00000000000000L) >> 56);
		 timeArray[index++] = (byte) ((stime & 0x00ff000000000000L) >> 48);
		 timeArray[index++] = (byte) ((stime & 0x0000ff0000000000L) >> 40);
		 timeArray[index++] = (byte) ((stime & 0x000000ff00000000L) >> 32);
		 timeArray[index++] = (byte) ((stime & 0x00000000ff000000L) >> 24);
		 timeArray[index++] = (byte) ((stime & 0x0000000000ff0000L) >> 16);
		 timeArray[index++] = (byte) ((stime & 0x000000000000ff00L) >> 8);
		 timeArray[index++] = (byte) ((stime & 0x00000000000000ffL));
	}

}

class GUI extends JFrame {
	private ImagePanel imagePanel;
	private JButton btnDisconnect;
	private JRadioButton btnMovie;
	private JRadioButton btnIdle;
	private JLabel lbDelay;
	private ButtonGroup group;
	private boolean firstCall = true;
	private String server;
	private int port;

	private ClientMonitor monitor;

	public GUI(String server, int port, ClientMonitor monitor) {
		super();
		this.server = server;
		this.port = port;
		this.monitor = monitor;
		imagePanel = new ImagePanel();

		this.setTitle("Operating at port : " + port);
		// The buttons are created
		btnMovie = new JRadioButton("Movie", true);
		btnMovie.addActionListener(new ButtonHandler(this, ClientMonitor.MOVIE_MODE));
		btnIdle = new JRadioButton("Idle", false);
		btnIdle.addActionListener(new ButtonHandler(this, ClientMonitor.IDLE_MODE));
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ButtonHandler(this, ClientMonitor.DISCONNECT));

		// Adds the radiobutton to a group
		ButtonGroup group = new ButtonGroup();
		group.add(btnMovie);
		group.add(btnIdle);

		// The buttons are added to a panel
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(btnMovie);
		buttonPane.add(btnIdle);
		buttonPane.add(Box.createHorizontalGlue());

		// The labels are added to a panel
		buttonPane.add(btnDisconnect);
		lbDelay = new JLabel("Delay Time");

		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.LINE_AXIS));
		labelPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		labelPane.add(new JLabel("Delay Time : "));
		labelPane.add(lbDelay);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(imagePanel, BorderLayout.CENTER);
		this.getContentPane().add(labelPane, BorderLayout.NORTH);
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		this.setLocationRelativeTo(null);
		this.pack();
	}

	public void setMode(byte b) {
		if (b == ClientMonitor.IDLE_MODE) {
			btnIdle.setSelected(true);
			btnMovie.setSelected(false);
		} else {
			btnIdle.setSelected(false);
			btnMovie.setSelected(true);
		}
	}

	/**
	 * Displays the sent image in the GUI, does not do this direct but invokes
	 * the inner thread in Swing
	 * 
	 * @param image
	 */
	public void refreshImage(byte[] image, long delay) {
		try {
			// In order to prevent swing from trying to display a corrupt
			// image
			// the image is stored in a temporary array
			byte[] tempImgArray = new byte[image.length];
			System.arraycopy(image, 0, tempImgArray, 0, image.length);
			lbDelay.setText(String.valueOf(delay));
			SwingUtilities.invokeLater(new Runnable() {
				// Show image when it is convenient
				public void run() {
					imagePanel.refresh(tempImgArray);
					pack();
					setVisible(true);
					setResizable(false);
				}
			});

			if (firstCall) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						pack();
						setVisible(true);
						firstCall = false;
						setResizable(false);
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendCommand(int command) {
		monitor.setCommand(command);
	}
}

class ImagePanel extends JPanel {
	ImageIcon icon;

	public ImagePanel() {
		super();
		icon = new ImageIcon();
		JLabel label = new JLabel(icon);
		add(label, BorderLayout.CENTER);
		this.setSize(200, 200);
	}

	public void refresh(byte[] data) {
		Image theImage = getToolkit().createImage(data);

		getToolkit().prepareImage(theImage, -1, -1, null);
		icon.setImage(theImage);
		icon.paintIcon(this, this.getGraphics(), 5, 5);
	}
}

class ButtonHandler implements ActionListener {

	GUI gui;
	private int command;

	public ButtonHandler(GUI gui, int command) {
		this.command = command;
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent evt) {
		gui.sendCommand(command);
	}
}
