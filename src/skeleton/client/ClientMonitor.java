package skeleton.client;

public class ClientMonitor {
	private byte[] jpeg;
	private boolean hasChanged;
	
	public ClientMonitor(){
		hasChanged = false;
	}
	public synchronized void putImage(byte[] jpeg){
		this.jpeg = jpeg;
		hasChanged = true;
		notifyAll();
	}
	
	public synchronized byte[] getImage() throws InterruptedException{
		while(!hasChanged){ wait();}
		hasChanged = false;
		return jpeg;
	}
}
