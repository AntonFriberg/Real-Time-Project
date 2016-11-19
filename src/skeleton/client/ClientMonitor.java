package skeleton.client;

import se.lth.cs.eda040.fakecamera.AxisM3006V;

public class ClientMonitor {
	private byte[] imgBuffer;
	private byte[] timeStampBuffer;
	private byte[] motionDetectBuffer;

	private boolean hasImage;

	public ClientMonitor() {
		hasImage = false;
		imgBuffer = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
		timeStampBuffer = new byte[AxisM3006V.TIME_ARRAY_SIZE];
		motionDetectBuffer = new byte[1];
	}

	/**
	 * Stores a byte array containing an image in a buffer. Does not care
	 * whether the last image has been displayed or not.
	 * 
	 * @param image,
	 *            an array of bytes containing the image
	 * @throws InterruptedException
	 */
	public synchronized void putImage(byte[] image, byte[] timeStamp, byte motionDetect) throws InterruptedException {
		System.arraycopy(image, 0, imgBuffer, 0, image.length);
		System.arraycopy(timeStamp, 0, timeStampBuffer, 0, timeStamp.length);
		motionDetectBuffer[0] = motionDetect;
		System.out.println("Put");
		hasImage = true;
		notifyAll();
	}

	/**
	 * Fetches an image if there is one to fetch.
	 * 
	 * @param image
	 * @throws InterruptedException
	 */
	public synchronized void getImage(byte[] image, byte[] timeStamp, byte[] motionDetect) throws InterruptedException {
		while (!hasImage)
			wait();
		System.arraycopy(imgBuffer, 0, image, 0, imgBuffer.length);
		System.arraycopy(timeStampBuffer, 0, timeStamp, 0, timeStampBuffer.length);
		motionDetect[0] = motionDetectBuffer[0];
		System.out.println("Fetch");
		hasImage = false;
		notifyAll();
	}
}
