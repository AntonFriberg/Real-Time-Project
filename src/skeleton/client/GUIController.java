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

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class GUIController {
	public GUIController(String server, String port){
		GUI gui = new GUI(server, Integer.parseInt(port));
	}
	
	class GUI extends JFrame {
		private ImagePanel imagePanel;
		private JButton button;
		private boolean firstCall = true;
		private String server;
		private int port;
		private byte [] jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
		private ClientMonitor monitor;
		
		
		public GUI(String server,int port) {
			super();
			this.server = server;
			this.port = port;
			imagePanel = new ImagePanel();
			button = new JButton("Get image");
			button.addActionListener(new ButtonHandler(this));
			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(imagePanel, BorderLayout.NORTH);
			this.getContentPane().add(button, BorderLayout.SOUTH);
			this.setLocationRelativeTo(null);
			this.pack();
			refreshImage();
			monitor = new ClientMonitor();
			ClientReceive receive = new ClientReceive(port, server, monitor);
			receive.run();
		}

		public void refreshImage() {
			try{
				jpeg = monitor.getImage();
			}
			catch(Exception E){
				
			}
			
			imagePanel.refresh(jpeg);
			if (firstCall) {
				this.pack();
				this.setVisible(true);
				firstCall = false;
			}
		}
	}
		// -------------------------------------------------------- PRIVATE METHODS

	class ButtonHandler implements ActionListener {

		GUI gui;

		public ButtonHandler(GUI gui) {
			this.gui = gui;
		}

		public void actionPerformed(ActionEvent evt) {
			gui.refreshImage();
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

}
