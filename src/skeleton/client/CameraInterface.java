package skeleton.client;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class CameraInterface extends Thread {
	private GUI gui;
	private ClientMonitor monitor;
	private byte[] jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
	private byte[] timeStamp = new byte[AxisM3006V.TIME_ARRAY_SIZE];
	private byte[] motionDetectStatus = new byte[0];
	private String server, port;

	public CameraInterface(String server, String port) {
		monitor = new ClientMonitor(); // The monitor between
		gui = new GUI(server, Integer.parseInt(port));
		this.server = server;
		this.port = port;
	}

	public void run() {
		ClientReceive clientReceive = new ClientReceive(server, Integer.parseInt(port), monitor);
		clientReceive.start();
		while (true) {
			try {
				monitor.getImage(jpeg, timeStamp, motionDetectStatus);
				gui.refreshImage(jpeg);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class GUI extends JFrame {
	private ImagePanel imagePanel;
	private JButton button;
	private boolean firstCall = true; 
	private String server;
	private int port;

	public GUI(String server, int port) {
		super();
		this.server = server;
		this.port = port;
		imagePanel = new ImagePanel();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(imagePanel, BorderLayout.NORTH);
		this.setLocationRelativeTo(null);
	}

	/**
	 * Displays the sent image in the GUI, does not do this direct but
	 * invokes the inner thread in Swing
	 * 
	 * @param image
	 */
	public void refreshImage(byte[] image) {
		try {
			// In order to prevent swing from trying to display a corrupt
			// image
			// the image is stored in a temporary array
			byte[] tempImgArray = new byte[image.length];
			System.arraycopy(image, 0, tempImgArray, 0, image.length);
			SwingUtilities.invokeLater(new Runnable() {
				//Show image when it is convenient
				public void run() {
					imagePanel.refresh(tempImgArray);

				}
			});
			
			if (firstCall) {
				//If it is the first call change the window
				this.pack();
				this.setVisible(true);
				firstCall = false;
				this.setResizable(false);
			}
		} catch (Exception e) {
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
