package skeleton.client;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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

import javax.swing.SwingUtilities;

public class GUI extends JFrame {

	private JButton btnDisconnect;
	private JButton btnConnect;
	private JButton btnMotionON;
	private JButton btnMotionOFF;
	private JButton btnShowAsynch;

	private JCheckBox btnAuto;

	private JLabel lbMode;
	private JLabel lbSynch;

	private ArrayList<CameraPanel> cameraPanelList;

	private boolean firstCall = true;
	private ClientMonitor monitor;

	public GUI(ClientMonitor monitor, int numberOfCameras) {
		super();
		this.monitor = monitor;
		// this.server = server;
		cameraPanelList = new ArrayList<CameraPanel>();

		for (int i = 0; i < numberOfCameras; i++) {
			cameraPanelList.add(new CameraPanel());
		}

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
		// The labels are added to a panel
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.LINE_AXIS));
		labelPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		labelPane.add(Box.createHorizontalGlue());
		labelPane.add(new JLabel("Motion : "));
		labelPane.add(lbMode);
		labelPane.add(new JLabel("Synchronous : "));
		labelPane.add(lbSynch);

		// The image panels show the cameras
		JPanel cameraPanelGroup = new JPanel();
		for (CameraPanel cameraPanel : cameraPanelList) {
			cameraPanelGroup.add(cameraPanel);
//		    cameraPanel.add(Box.createHorizontalStrut(320));
//		    cameraPanel.add(Box.createVerticalStrut(300));
		}

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(cameraPanelGroup, BorderLayout.CENTER);
		this.getContentPane().add(labelPane, BorderLayout.NORTH);
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		this.setLocationRelativeTo(null);
		this.pack();
//		this.setVisible(true);

		
	}



	public void setMode(boolean motion) {
		if (!motion) {
			lbMode.setText("OFF");
		} else {
			lbMode.setText("ON");
		}
	}
	
	public void setMotionIndicator(boolean motion){
		if (motion) {
			lbMode.setText("ON");
		} else {
			lbMode.setText("OFF");
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
			
			if(cameraID > cameraPanelList.size()){
				return;
			}
			
			CameraPanel tempPanel = cameraPanelList.get(cameraID);
			
			// In order to prevent swing from trying to display a corrupt
			// image
			// the image is stored in a temporary array
			byte[] tempImgArray = new byte[image.length];
			System.arraycopy(image, 0, tempImgArray, 0, image.length);
			
			SwingUtilities.invokeLater(new Runnable() {
				// Show image when it is convenient
				public void run() {
					tempPanel.refresh(tempImgArray, delay);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void firstCallInitiate(){
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
	
	public CameraPanel(){
		super();
		imagePanel = new ImagePanel();
		lbDelay = new JLabel("Delay");
		setLayout(new BorderLayout());
		add(lbDelay, BorderLayout.NORTH);
		add(imagePanel, BorderLayout.SOUTH);
	}
	
	public void refresh(byte[] data, long delay){
		lbDelay.setText("Delay Time : " + String.valueOf(delay));
		imagePanel.refresh(data);
	}
}


class ImagePanel extends JPanel {
	ImageIcon icon;
	JLabel lbDelay;


	public ImagePanel() {
		super();
		icon = new ImageIcon();
		JLabel label = new JLabel(icon);
		add(label, BorderLayout.SOUTH);
		this.setSize(320,240);

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
