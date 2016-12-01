package skeleton.client;

import java.util.ArrayList;

public class GuiController extends Thread{
	private int numberOfCameras = 0;
	private boolean showAsynchronous = false;
	private ArrayList<Camera> camWaitingForImgList;
	private ArrayList<Camera> camReadyForDisplayList;

	public GuiController(ArrayList<String> receivePorts, ArrayList<String> sendPorts) {
		numberOfCameras = receivePorts.size() == sendPorts.size() ? receivePorts.size() : 0;
		camWaitingForImgList = new ArrayList<Camera>();
		camReadyForDisplayList = new ArrayList<Camera>();
		// Adds all the cameras specified to a list

		for (int i = 0; i < numberOfCameras; i++) {

			int receivePort = Integer.parseInt(receivePorts.get(i));
			int sendPort = Integer.parseInt(sendPorts.get(i));
			camWaitingForImgList.add(new Camera(receivePort, sendPort));
			// Starts the receiving and sending threads
			new ClientReceive("localhost", receivePort, camWaitingForImgList.get(i).getMonitor()).start();
			new ClientSend("localhost", sendPort, camWaitingForImgList.get(i).getMonitor()).start();
		}
	}

	public void run() {
		long setRelativeTime;
		while (true) {
			try {
				getImage();
				tryWaitForOther();
				setRelativeTime = getRelativeTime();
				if (setRelativeTime == -1) {
					showAsynchronous = true;
				}
				for (Camera cam : camReadyForDisplayList) {
					if (showAsynchronous)
						cam.show(0);
					else
						cam.show(setRelativeTime);
					camWaitingForImgList.add(cam);
				}

				camReadyForDisplayList = new ArrayList<Camera>();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// We want to set the images taken shown relative to the others
	private long calculateRelativeTime(long firstImgTaken, Camera cam) {
		long takenTime = cam.getTimeStamp();
		if (firstImgTaken > takenTime) {
			firstImgTaken = takenTime;
		}
		return firstImgTaken;
	}

	private long getRelativeTime() {
		long firstImgTaken = Long.MAX_VALUE;
		for (Camera cam : camReadyForDisplayList) {
			firstImgTaken = calculateRelativeTime(firstImgTaken, cam);
		}
		if (firstImgTaken < System.currentTimeMillis()) {
			return firstImgTaken;
		} else {
			// Something went wrong
			return -1;
		}
	}

	private void getImage() throws InterruptedException {
		if (camWaitingForImgList.size() == 0)
			return;
		boolean received = false;
		int index = 0;
		Camera tempCam;

		while (!received) {
			tempCam = camWaitingForImgList.get(index);
			received = tempCam.getMonitor().tryGetImage(tempCam);
			if (!received) {
				if (index == camWaitingForImgList.size() - 1) {
					index = 0;
				}
				Thread.sleep(50);
			}
		}
		camReadyForDisplayList.add(camWaitingForImgList.remove(index));
	}

	private void tryWaitForOther() throws InterruptedException {
		boolean received;
		Camera tempCam;
		int size = camWaitingForImgList.size();
		int index = 0;
		while (index < size) {
			tempCam = camWaitingForImgList.get(index);
			received = tempCam.getMonitor().getImage(tempCam, ClientMonitor.SYNCHRONIZATION_THRESHOLD);
			if (received) {
				camReadyForDisplayList.add(camWaitingForImgList.remove(index));
				size--;
			} else {
				index++;
			}
		}
	}
}

