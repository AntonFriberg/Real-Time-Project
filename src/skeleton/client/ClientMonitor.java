package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientMonitor {
	private byte[] buffer;
	private boolean hasImage;
	
	public ClientMonitor(){
		hasImage = false;
		buffer = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
	}
	public synchronized void putImage(byte[] image) throws InterruptedException{
		while(hasImage) wait();
		System.arraycopy(image, 0, buffer, 0, image.length);
		hasImage = true;
		notifyAll();
	}
	
	public synchronized void getImage(byte[] image) throws InterruptedException{
		while(!hasImage) wait();
		System.arraycopy(buffer, 0, image, 0, image.length);
		hasImage = false;
		notifyAll();
	}
}
