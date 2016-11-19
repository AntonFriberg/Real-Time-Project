package skeleton.demo;

import skeleton.server.JPEGHTTPServer;
import skeleton.client.CameraController;
//import skeleton.client.JPEGHTTPClient;
import skeleton.client.CameraInterface;

public class JPEGHTTPDemo {

	public static void main(String[] args) {
		Server1 s1 = new Server1();
		Server2 s2 = new Server2();
		s1.start();
		s2.start();
//		Client c = new Client();
//		c.start();
//		CameraInterface gui = new CameraInterface("localhost","6077");
//		gui.start();
		CameraController.main(new String[]{"localhost","6077"});
	}
	
	private static class Server1 extends Thread {
		public void run() {
			JPEGHTTPServer.main(new String[] {"6077"});
		}
	}
	
	private static class Server2 extends Thread {
		public void run() {
			JPEGHTTPServer.main(new String[] {"6078"});
		}
	}
	
	private static class Client extends Thread {
		public void run() {
			//JPEGHTTPClient.main(new String[] {"localhost", "6078"});
		}
	}
}
