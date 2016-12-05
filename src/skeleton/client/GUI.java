package skeleton.client;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

/**
 * 
 * @author Olof Rubin and Erik Andersson
 *
 */
public class GUI extends JFrame {

	private JButton btnDisconnect;
	private JButton btnConnect;
	private JButton btnMotionON;
	private JButton btnMotionOFF;
	private JButton btnShowAsynch;
	private JCheckBox btnAuto;
	private JLabel lbMode;
	private JLabel lbSynch;
	private JLabel lbTriggerID;

	private ArrayList<CameraPanel> cameraPanelList;
	private ClientMonitor monitor;
	private int numberOfCameras;

	public GUI(ClientMonitor monitor, int numberOfCameras) {
		super();

		this.numberOfCameras = numberOfCameras;
		this.monitor = monitor;
		// this.server = server;
		cameraPanelList = new ArrayList<CameraPanel>();

		for (int i = 0; i < numberOfCameras; i++) {
			cameraPanelList.add(new CameraPanel(i));
		}

		// this.setTitle("Operating at port : " + port);
		// The buttons are created
		btnMotionON = new JButton("Motion On" + "");
		btnMotionON.addActionListener(new ButtonHandler(this, ClientMonitor.MOTION_ON));
		btnMotionOFF = new JButton("Motion Off");
		btnMotionOFF.addActionListener(new ButtonHandler(this, ClientMonitor.MOTION_OFF));
		btnAuto = new JCheckBox("Auto", true);
		btnAuto.addActionListener(new CheckBoxHandler(btnAuto, this));
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ButtonHandler(this, ClientMonitor.DISCONNECT));
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ButtonHandler(this, ClientMonitor.CONNECT));
		btnShowAsynch = new JButton("Synchronous/Asynchronous");
		btnShowAsynch.addActionListener(event -> {
			monitor.changeSynchronousMode();
		});

		// The buttons are added to a panel
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(btnAuto);
		buttonPane.add(btnMotionON);
		buttonPane.add(btnMotionOFF);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(btnShowAsynch);
		buttonPane.add(btnDisconnect);
		buttonPane.add(btnConnect);

		lbMode = new JLabel("Mode");
		lbSynch = new JLabel("Sync");
		lbTriggerID = new JLabel();
		// The labels are added to a panel
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.LINE_AXIS));
		labelPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		labelPane.add(new JLabel("Motion : "));
		labelPane.add(lbMode);
		labelPane.add(lbTriggerID);
		labelPane.add(Box.createHorizontalGlue());
		labelPane.add(new JLabel("Synchronous : "));
		labelPane.add(lbSynch);

		// The image panels show the cameras
		JPanel cameraPanelGroup = new JPanel();
		for (CameraPanel cameraPanel : cameraPanelList) {
			cameraPanelGroup.add(cameraPanel);
			// cameraPanel.add(Box.createHorizontalStrut(320));
			// cameraPanel.add(Box.createVerticalStrut(300));
		}

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(cameraPanelGroup, BorderLayout.CENTER);
		this.getContentPane().add(labelPane, BorderLayout.NORTH);
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		this.setLocationRelativeTo(null);
		this.setMinimumSize(new Dimension(1400,600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();

	}

	public void setMode(boolean motion) {
		if (!motion) {
			lbMode.setText("OFF");
		} else {
			lbMode.setText("ON");
		}
	}

	public void setSynchIndicator(boolean synch) {
		if (synch) {
			lbSynch.setText("ON");
		} else {
			lbSynch.setText("OFF");
		}
	}

	public void setMotionIndicator(boolean motion) {
		if (motion) {
			lbMode.setText("ON");
		} else {
			lbMode.setText("OFF");
		}
	}

	public void displayMotionTriggerID(int cameraID) {
		if (cameraID >= 0) {
			lbTriggerID.setText(" \t \t TriggerID : " + String.valueOf(cameraID));
		}
	}

	/**
	 * Displays the sent image in the GUI, does not do this direct but invokes
	 * the inner thread in Swing
	 * 
	 * @param image
	 */
	public void refreshImage(byte[] image, long delay, int cameraID) {
		try {

			if (cameraID > cameraPanelList.size()) {
				return;
			}

			CameraPanel tempPanel = cameraPanelList.get(cameraID);
			// In order to prevent swing from trying to display a corrupt
			// image
			// the image is stored in a temporary array
			byte[] tempImgArray = new byte[image.length];
			System.arraycopy(image, 0, tempImgArray, 0, image.length);
			int width = this.getWidth();
			int height = this.getHeight();
			SwingUtilities.invokeLater(new Runnable() {
				// Show image when it is convenient
				public void run() {
					tempPanel.refresh(tempImgArray, delay, numberOfCameras, width, height);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void firstCallInitiate() {
		SwingUtilities.invokeLater(new Runnable() {
			// Show image when it is convenient
			public void run() {
				pack();
				setVisible(true);
			}
		});
	}

	public void sendCommand(int command) {
		try {
			monitor.setCommand(command);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class CameraPanel extends JPanel {
	ImagePanel imagePanel;
	JLabel lbDelay;
	int cameraID;
	public CameraPanel(int cameraID) {
		super();
		this.cameraID = cameraID;
		imagePanel = new ImagePanel();
		lbDelay = new JLabel("Delay");
		setLayout(new BorderLayout());
		add(lbDelay, BorderLayout.NORTH);
		add(imagePanel, BorderLayout.SOUTH);
	}

	public void refresh(byte[] data, long delay, int numberOfCameras,int x, int y) {
		lbDelay.setText("Camera : " + cameraID + "\t Delay Time : " + String.valueOf(delay));
		imagePanel.refresh(data, numberOfCameras, x, y);
	}
}

class ImagePanel extends JPanel {
	ImageIcon icon;
	JLabel lbDelay;
	JLabel lbImg;
	
	public ImagePanel() {
		super();
		icon = new ImageIcon();
		lbImg = new JLabel(icon);
		add(lbImg, BorderLayout.SOUTH);
		this.setSize(320, 240);

	}
	
	public void refresh(byte[] data, int numofcams, int x, int y) {
		InputStream is = new ByteArrayInputStream(data);
		try {
			/*
			 * converts the byte array into a BufferedImage, in order to be able
			 * to resize it
			 */
			BufferedImage bufferedImage = ImageIO.read(is);
			int x1 = 7 * x / 8;

			int w = bufferedImage.getWidth();
			int h = bufferedImage.getHeight();
			int x2 = (int) x1 / numofcams;
			int y2 = (int) x2 * h / w;

			BufferedImage after = resizeImage(bufferedImage, x2, y2);
			lbImg.setSize(x2, y2);
					icon.setImage(after);
			icon.paintIcon(this, this.getGraphics(), 5, 5);
		} catch (IOException e) {
			System.out.println("Image Error");
		}
		

	}

	private BufferedImage resizeImage(final Image image, int width, int height) {
		final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.Src);
		// below three lines are for RenderingHints for better image quality at
		// cost of higher processing time
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		graphics2D.dispose();
		return bufferedImage;
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

class ButtonHandler implements ActionListener {
	/*
	 * Handles connection button press
	 */
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
