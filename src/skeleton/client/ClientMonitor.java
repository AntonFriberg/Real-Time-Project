package skeleton.client;

public class ClientMonitor {
	private byte[] buffer;
	private boolean hasImage;
	
	public ClientMonitor(){
		hasImage = false;
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
