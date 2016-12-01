package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class Camera {
	private byte[] jpeg;
	private long timeStamp;
	private boolean motionDetect;
	private int cameraID;
	
	public Camera(int cameraID) {
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
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Camera){
			return this.cameraID == ((Camera) o).cameraID;
		}
		return false;
	}



}
