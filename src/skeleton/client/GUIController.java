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

public class GUIController extends Thread {
	private GUI gui;
	private ClientMonitor monitor;
	private byte[] jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
	
	public GUIController(String server, String port) {
		monitor = new ClientMonitor(); // The monitor between
		gui = new GUI(server, Integer.parseInt(port));
		ClientReceive clientReceive = new ClientReceive(server, Integer.parseInt(port), monitor);
		clientReceive.start();
	}

	public void run() {
		while (true){
			try {
				monitor.getImage(jpeg);
				gui.refreshImage(jpeg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
	
	