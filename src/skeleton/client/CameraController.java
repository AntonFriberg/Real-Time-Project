package skeleton.client;

public class CameraController {
	public static void main(String[] args){
		if(args.length >= 3){
			String server = args[0];
			String receivePort = args[1];
			String sendPort = args[2];
			CameraInterface camera1 = new CameraInterface(server, receivePort, sendPort);
//			CameraInterface camera2 = new CameraInterface(server,port);
			camera1.start();
//			camera2.start();			
		}
	}
}
