package skeleton.client;

import java.util.ArrayList;

public class CameraController {
	public static void main(String[] args) {
		if (args.length >= 3) {

			ArrayList<String> sendPorts = new ArrayList<String>();
			ArrayList<String> recPorts = new ArrayList<String>();

			sendPorts.add(args[4]);
			sendPorts.add(args[5]);
			sendPorts.add(args[6]);
			
			recPorts.add(args[1]);
			recPorts.add(args[2]);
			recPorts.add(args[3]);

			GuiController camera1 = new GuiController(recPorts, sendPorts);
			camera1.start();

			// CameraInterface camera1 = new CameraInterface(server,
			// receivePort, sendPort);
			// CameraInterface camera2 = new CameraInterface(server,port);
			// camera1.start();
			// camera2.start();
		}
	}
}
