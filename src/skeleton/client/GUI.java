package skeleton.client;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JLabel;

import javax.swing.JPanel;

import javax.swing.SwingUtilities;

public class GUI extends JPanel {

	private ImagePanel imagePanel;
	
	private JButton btnDisconnect;
	private JButton btnConnect;
	private JButton btnMotionON;
	private JButton btnMotionOFF;

	private JCheckBox btnAuto;
	private JLabel lbDelay;
	private JLabel lbMode;
	private ButtonGroup group;
	private boolean firstCall = true;
	private ClientMonitor monitor;
	private int cameraID;

	public GUI(int port, ClientMonitor monitor, int cameraID) {
		super();
		this.cameraID = cameraID;
		this.monitor = monitor;
		// this.server = server;
		imagePanel = new ImagePanel();

		// this.setTitle("Operating at port : " + port);
		// The buttons are created
		btnMotionON = new JButton("Motion On" + "");
		btnMotionON.addActionListener(new ButtonHandler(this, ClientMonitor.MOVIE_MODE));
		btnMotionOFF = new JButton("Motion Off");
		btnMotionOFF.addActionListener(new ButtonHandler(this, ClientMonitor.IDLE_MODE));
		btnAuto = new JCheckBox("Auto", true);
		btnAuto.addActionListener(new CheckBoxHandler(btnAuto, this));
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ButtonHandler(this, ClientMonitor.DISCONNECT));
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ButtonConnectHandler(this, ClientMonitor.CONNECT));

		// The buttons are added to a panel
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(btnAuto);
		buttonPane.add(btnMotionON);
		buttonPane.add(btnMotionOFF);
		buttonPane.add(Box.createHorizontalGlue());

		buttonPane.add(btnDisconnect);
		buttonPane.add(btnConnect);
		lbDelay = new JLabel("Delay Time");
		lbMode = new JLabel("Mode");

		// The labels are added to a panel
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.LINE_AXIS));
		labelPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		labelPane.add(new JLabel("Delay Time : "));
		labelPane.add(lbDelay);
		labelPane.add(Box.createHorizontalGlue());
		labelPane.add(new JLabel("Motion : "));
		labelPane.add(lbMode);

		this.setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(imagePanel, BorderLayout.CENTER);
		this.add(labelPane, BorderLayout.NORTH);
		this.add(buttonPane, BorderLayout.SOUTH);
		//
		// this.getContentPane().setLayout(new BorderLayout());
		// this.getContentPane().add(imagePanel, BorderLayout.CENTER);
		// this.getContentPane().add(labelPane, BorderLayout.NORTH);
		// this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		// this.setLocationRelativeTo(null);
		// this.pack();
	}

	public JPanel getFrame() {
		return this;
	}

	public void setMode(boolean motion) {
		if (!motion) {
			lbMode.setText("OFF");
		} else {
			lbMode.setText("ON");
		}
	}

	/**
	 * Displays the sent image in the GUI, does not do this direct but invokes
	 * the inner thread in Swing
	 * 
	 * @param image
	 */
	public void refreshImage(byte[] image, long waitFor, long delay, boolean motion) {
		try {
			// In order to prevent swing from trying to display a corrupt
			// image
			// the image is stored in a temporary array
			byte[] tempImgArray = new byte[image.length];
			System.arraycopy(image, 0, tempImgArray, 0, image.length);
			lbDelay.setText(String.valueOf(delay));
			if (motion) {
				lbMode.setText("ON");
			} else {
				lbMode.setText("OFF");
			}
			SwingUtilities.invokeLater(new Runnable() {
				// Show image when it is convenient
				public void run() {
					imagePanel.refresh(tempImgArray);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendCommand(int command) {
		try {
			monitor.setCommand(command, cameraID);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

class CheckBoxHandler implements ActionListener {
	GUI gui;
	JCheckBox checkBox;

	public CheckBoxHandler(JCheckBox checkBox, GUI gui) {
		this.checkBox = checkBox;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (checkBox.isSelected()) {
			gui.sendCommand(ClientMonitor.AUTO_MODE);
		} else {
			gui.sendCommand(ClientMonitor.MANUAL_MODE);
		}
	}
}

class ButtonConnectHandler implements ActionListener {
	GUI gui;
	private int command;

	public ButtonConnectHandler(GUI gui, int command) {
		this.command = command;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		gui.sendCommand(command);
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
