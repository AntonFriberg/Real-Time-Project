package skeleton.client;

public class CameraController {
	public static void main(String[] args){
		if(args.length >= 2){
			String server = args[0];
			String port = args[1];
			CameraInterface camera1 = new CameraInterface(server, port);
			CameraInterface camera2 = new CameraInterface(server,port);
			camera1.start();
			camera2.start();			
		}
	}
}
