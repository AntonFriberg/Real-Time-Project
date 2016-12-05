package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

/**
 * 
 * @author Olof Rubin and Erik Andersson
 *
 */
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
	
	/**
	 * Get the identification of the camera through a number
	 * @return the integer representation of this cameraID
	 */
	public int getID(){
		return cameraID;
	}

	/**
	 * Get the image that is stored in this object
	 * @return a byte array containing image data stored as a JPEG
	 */
	public byte[] getJpeg() {
		return jpeg;
	}

	/**
	 * Sets the mode of capture that this image was taken with
	 * @param motionDetect true means motion on, false means motion off
	 */
	public void setMotionDetect(boolean motionDetect) {
		this.motionDetect = motionDetect;
	}

	/**
	 * Gets the mode with which the image in this object was captured with
	 * @return the state of motion mode 
	 */
	public boolean motionDetect() {
		return motionDetect;
	}

	/**
	 * Sets the timestamp indicating when the image in this object was taken
	 * @param timeStamp the time of capture as a long
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * Gets the time that the image was taken
	 * @return a long containing capture time
	 */
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
