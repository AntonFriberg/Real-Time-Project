package skeleton.client;
import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

@SuppressWarnings("serial")
public class Test extends JFrame implements Runnable {
  ImageIcon icon;
  boolean firstCall = true;

  public static void main(String[] args) {
    Test viewer = new Test();
    (new Thread(viewer)).start();
  }

  public Test() {
    super();
    getContentPane().setLayout(new BorderLayout());
    icon = new ImageIcon();
    JLabel label = new JLabel(icon);
    add(label, BorderLayout.CENTER);
    this.pack();
    this.setSize(640, 480);
    this.setVisible(true);
  }

  public void run() {
    AxisM3006V cam = new AxisM3006V();
    cam.init();
    cam.connect();
    while (true) {
      final byte[] jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
      cam.getJPEG(jpeg, 0);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          refreshImage(jpeg);
        }
      });
    }
    //cam.close();
    //cam.destroy();
  }

  public void refreshImage(byte[] jpeg) {
    Image image = getToolkit().createImage(jpeg);
    getToolkit().prepareImage(image,-1,-1,null);
    icon.setImage(image);
    icon.paintIcon(this, this.getGraphics(), 0, 0);
  }
}