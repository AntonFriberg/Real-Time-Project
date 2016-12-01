package skeleton.client;

import java.util.ArrayList;

public class CameraController {
	public static void main(String[] args){
		if(args.length >= 3){
			String receivePort = args[2];
			String sendPort = args[1];
			
			ArrayList<String> sendPorts = new ArrayList<String>();
			ArrayList<String> recPorts = new ArrayList<String>();
			
			sendPorts.add(sendPort);
			recPorts.add(receivePort);
			
			GuiController camera1 = new GuiController(recPorts, sendPorts);
			camera1.start();
			
//			CameraInterface camera1 = new CameraInterface(server, receivePort, sendPort);
//			CameraInterface camera2 = new CameraInterface(server,port);
//			camera1.start();
//			camera2.start();			
		}
	}
}
