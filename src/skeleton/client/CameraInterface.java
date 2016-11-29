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
import javax.swing.JOptionPane;
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
		return System.currentTimeMillis() - convertTime(timeArray);
	}

	private long convertTime(byte[] timeArray) {
		long time = 0;
		for (int i = 0; i < timeArray.length; i++) {
			time += ((long) timeArray[i] & 0xffL) << (8 * (7 - i));
		}
		return time;
	}

}

class GUI extends JFrame {
	private ImagePanel imagePanel;
	private JButton btnDisconnect;
	private JButton btnConnect;
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
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ButtonConnectHandler(this));

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

		buttonPane.add(btnDisconnect);
		buttonPane.add(btnConnect);
		lbDelay = new JLabel("Delay Time");

		// The labels are added to a panel
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

class ButtonConnectHandler implements ActionListener {
	GUI gui;

	public ButtonConnectHandler(GUI gui) {
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object[] possibilities = { "6077", "6078", "6079" };
		String s = (String) JOptionPane.showInputDialog(gui, "Choose Port:\n", "Connect to camera",
				JOptionPane.PLAIN_MESSAGE, null, possibilities, "6077");

		// If a string was returned, say so.
		if ((s != null) && (s.length() > 0)) {
			// setLabel("Green eggs and... " + s + "!");
			return;
		}

		// If you're here, the return value was null/empty.
		// setLabel("Come on, finish the sentence!");
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
