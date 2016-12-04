/*
 * Real-time and concurrent programming
 *
 */

import java.io.*;

import se.lth.cs.eda040.realcamera.*;
import http.*;

public class Main {

    public static void main(String[]args) {
	AxisM3006V camera = new AxisM3006V();
	camera.init();
        JPEGHTTPServer httpServer = new JPEGHTTPServer(camera, 6077);
	try {
	    httpServer.handleRequests();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	camera.destroy();
    }

} 
