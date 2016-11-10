package skeleton.demo;

import skeleton.server.JPEGHTTPServer;
import skeleton.client.JPEGHTTPClient;

public class JPEGHTTPDemo {

	public static void main(String[] args) {
		Server1 s1 = new Server1();
		s1.start();
		Server2 s2 = new Server2();
		s2.start();
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Client c = new Client();
		c.start();
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
			JPEGHTTPClient.main(new String[] {"localhost", "6077"});
		}
	}
}
