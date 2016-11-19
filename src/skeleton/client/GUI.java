package skeleton.client;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

//	class ButtonHandler implements ActionListener {
//
//		GUI gui;
//
//		public ButtonHandler(GUI gui) {
//			this.gui = gui;
//		}
//
//		public void actionPerformed(ActionEvent evt) {
//			gui.refreshImage();
//		}
//	}

public class GUI extends JFrame {
	private ImagePanel imagePanel;
	private JButton button;
	private boolean firstCall = true;
	private String server;
	private int port;

	/**
	 * 
	 * @param server
	 * @param port
	 * @param monitor
	 */
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
	 * Displays the sent in image in the GUI, does not do this direct but invokes the inner thread in Swing
	 * 
	 * @param image
	 */
	public void refreshImage(byte[] image) {
		try {
			// In order to prevent swing from trying to display a corrupt image
			// the image is stored in a temporary array
			byte[] tempImgArray = new byte[image.length];
			System.arraycopy(image, 0, tempImgArray, 0, image.length);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					imagePanel.refresh(tempImgArray);
					
				}
			});
			if (firstCall) {
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