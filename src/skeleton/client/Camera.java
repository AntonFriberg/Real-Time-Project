package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class Camera {
	private GUI gui;
	private byte[] jpeg;
	private long timeStamp;
	private boolean motionDetect;

	private ClientMonitor monitor;
	private int sendPort, recPort;

	
	
	public Camera(int sendPort, int recPort) {
		this.sendPort = sendPort;
		this.recPort = recPort;
		this.jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
		timeStamp = 0;
		motionDetect = false;
		this.monitor = new ClientMonitor();
		this.gui = new GUI(sendPort, monitor);
	}

	public byte[] getJpeg() {
		return jpeg;
	}
	
	public void setMotionDetect(boolean motionDetect){
		this.motionDetect = motionDetect;
	}

	public boolean motionDetect() {
		return motionDetect;
	}

	public void setTimeStamp(long timeStamp){
		this.timeStamp = timeStamp;
	}
	
	public long getTimeStamp(){
		return timeStamp;
	}
	
	public ClientMonitor getMonitor() {
		return monitor;
	}

	public void setSendPort(int sendPort) {
		this.sendPort = sendPort;
	}

	public int getSendPort() {
		return sendPort;
	}

	public void setRecPort(int recPort){
		this.recPort = recPort;
	}
	public int getRecPort() {
		return recPort;
	}
	
	public void show(long relativeTime){
		gui.refreshImage(jpeg, (relativeTime - timeStamp), getDelay());
		gui.setMode(motionDetect);
		System.out.println(motionDetect);
	} 
	
	private long getDelay() {
		return System.currentTimeMillis() - timeStamp;
	}

}
