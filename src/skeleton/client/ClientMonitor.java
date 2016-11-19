package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientMonitor {
	private byte[] buffer;
	private boolean hasImage;
	
	
	public ClientMonitor(){
		hasImage = false;
		buffer = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
	}
	
	/**
	 * Stores a byte array containing an image in a buffer. No image can be stored if the previous one has not been collected.
	 * @param image, an array of bytes containing the image
	 * @throws InterruptedException
	 */
	public synchronized void putImage(byte[] image) throws InterruptedException{
		while(hasImage) wait();
		System.arraycopy(image, 0, buffer, 0, image.length);
		hasImage = true;
		notifyAll();
	}
	
	/**
	 * Fetches an image if there is one to fetch.
	 * @param image
	 * @throws InterruptedException
	 */
	public synchronized void getImage(byte[] image) throws InterruptedException{
		while(!hasImage) wait();
		System.arraycopy(buffer, 0, image, 0, buffer.length);
		hasImage = false;
		notifyAll();
	}
}
