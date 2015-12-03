package csoh.reference.usb4java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.glassfish.tyrus.server.Server;

@ServerEndpoint("/usbhost")
public class WebsocketServerEndpoint implements UsbMessageListener{
    private static final String CONNECT_VENDOR_USB_TO_ACCESSORY = "CONNECT_VENDOR_USB_TO_ACCESSORY";
    private static final String CONNECT_USB_ACCESSORY = "CONNECT_USB_ACCESSORY";
    private static final String SEND_USB = "SEND_USB";

    private static Server server = null;

    UsbTestLow usbController = UsbTestLow.getInstance();
    
    HashMap<String, Session> sessions = new HashMap<String, Session>();
    
    private void doTest() {
	try {
	    for (int i = 0; i < 1000000; i++)
		broadCast("mess");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @OnOpen
    public void open(Session session) {
	System.out.println("Open session " + session);
	System.out.println("Websocket this> " + this);
	sessions.put(session.getId(), session);
	doTest();
    }

    @OnClose
    public void close(Session session) {
	System.out.println("Close session " + session);
	sessions.remove(session.getId());
	usbController.closeUsbObject();
	usbController.exit();
    }

    @OnError
    public void onError(Throwable error) {
	System.out.println("Error  " + error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
	System.out.println("Message from session " + session + ":" + message);
	String[] parts = message.split(":");
	int len = parts.length;
	String cmd = (len == 1) ? parts[0] : "";
	String arg = (len > 1 )? parts[1] : "";
	
	try {
	    switch (cmd) {
	    case CONNECT_VENDOR_USB_TO_ACCESSORY:
		System.out.println("Preparing to connect to Vendor USB");
		short tab3_vendorid = (short) 0x04e8; // Tab3,
		usbController.setupUsb(tab3_vendorid);
		int result = usbController.androidDeviceToAccessoryMode("CsohManufacturer", "CsohModel", "CsohDescription", "1.0", "http://www.mycompany.com", "SerialNumber");
		break;

	    case CONNECT_USB_ACCESSORY:
		System.out.println("Connect to usb accessory");

		usbController.setupUsbForAndroid();
		System.out.println("Begin communication");
		Thread t = new Thread(usbController);
		t.start();
		// transferBulkData(usbForCommunication.getHandle(),
		// END_POINT_OUT_GOOGLE , "Hello how are you?", 100000);
		// System.out.println("Transffered, Sleeping 1000 sec");
		// Thread.sleep(1000000);
		//t.join();
		//usbControl.closeUsbObject();
		System.out.println("Fin");
		break;
		
	    case SEND_USB :
		usbController.sendMessageToAndroid(arg);
		break;
	    default:
		System.out.println("Unrecognized command '" + message + "'");
		break;

	    }
	} catch (DeviceNotFoundException e) {
	    e.printStackTrace();
	} catch (GeneralException ge) {
	    ge.printStackTrace();
	}

    }

    public void broadCast(String message) throws IOException {
	  for (Session s : sessions.values()) {
	    s.getBasicRemote().sendText(message);
	  }
    }
    public static void startServer(String url, int port, String path) {
	server = new Server(url, port, path, WebsocketServerEndpoint.class);
	try {
	    server.start();
	} catch (DeploymentException e) {
	    e.printStackTrace();
	}
    }

    public static void stopServer() {
	if (server != null)
	    server.stop();
    }

    public static void main(String[] args) {
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

    @Override
    public void onMessageFromUsb(String message) {
	try {
	    broadCast(message);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }

}
