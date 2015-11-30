package csoh.reference.usb4java;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.glassfish.tyrus.server.Server;


@ServerEndpoint("/usbhost")
public class WebsocketServerEndpoint {
    private static Server server = null;
    
    @OnOpen
    public void open(Session session) {
	System.out.println("Open session " + session);
    }

    @OnClose
    public void close(Session session) {
	System.out.println("Close session " + session);
    }

    @OnError
    public void onError(Throwable error) {
	System.out.println("Error  " + error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
	System.out.println("Message from session " + session + ":" + message);
    }
    
    public static void startServer(String url, int port, String path){
	 server = new Server(url, port, path, WebsocketServerEndpoint.class);
	 try {
	    server.start();
	} catch (DeploymentException e) {
	    e.printStackTrace();
	}
    }
    public static void stopServer(){
	if(server != null) server.stop();
    }
    
    public static void main(String[] args){
	// Local test 

	    try {
		startServer("localhost", 8025, "/websocket");
	      
	        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print("Please press a key to stop the server.");
	        reader.readLine();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
		stopServer();
	    }
    }
}
