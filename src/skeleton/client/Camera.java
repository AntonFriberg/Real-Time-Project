package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class Camera implements Comparable{
	private byte[] jpeg;
	private long timeStamp;
	private boolean motionDetect;
	private int cameraID;
	
	private ClientMonitor monitor;
	private int sendPort, recPort;

	
	public Camera(int cameraID){
		this.cameraID = cameraID;
		this.jpeg = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
		timeStamp = 0;
		motionDetect = false;
	}
	
	public int getID(){
		return cameraID;
	}

	public byte[] getJpeg() {
		return jpeg;
	}

	public void setMotionDetect(boolean motionDetect) {
		this.motionDetect = motionDetect;
	}

	public boolean motionDetect() {
		return motionDetect;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * Shows the image that is received but does so with a delay indicated by
	 * the difference in time between this and the image which was displayed
	 * first
	 * 
	 * @param relativeTime
	 */
//	public void show(long relativeTime) {
//		if (relativeTime == 0) {
//			gui.refreshImage(jpeg, 0, getDelay());
//			System.out.println("Showing Image with :  0 seconds delay");
//		}
//
//		else {
//			gui.refreshImage(jpeg, (timeStamp - relativeTime), getDelay());
//			System.out.println("Showing Image with :  " + (timeStamp - relativeTime) + " seconds delay");
//		}
//		gui.setMode(motionDetect);
//		System.out.println(motionDetect);
//	}

	private long getDelay() {
		return System.currentTimeMillis() - timeStamp;
	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof Camera){
			return Long.compare(timeStamp, ((Camera)o).timeStamp);
		}
		return 0;
	}

}
