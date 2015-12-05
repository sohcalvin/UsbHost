package csoh.reference.usb4java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class WebsocketServerEndpoint implements UsbMessageListener {
    private static final String USBCON_VENDOR_SWITCH_TO_ACCESSORY = "USBCON_VENDOR_SWITCH_TO_ACCESSORY";
    private static final String USBCON_ACCESSORY = "USBCON_ACCESSORY";
    private static final String SEND_USB = "SEND_USB";

    private Server server = null;

    UsbTestLow usbController = UsbTestLow.getInstance();

    HashMap<String, Session> sessions = new HashMap<String, Session>();

    private void doTest() {
	    for (int i = 0; i < 1000000; i++)
		broadCast("mess");
    }

    @OnOpen
    public void open(Session session) {
	System.out.println("Websocket session opened sessionId='" + session.getId() + "', instance='" + this.toString() + "'");
	sessions.put(session.getId(), session);
	//doTest();
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
	System.out.println("Mess='" + message + "' Session='" + session + "'");
	String[] parts = message.split(":");
	int len = parts.length;
	String cmd = (len >= 1) ? parts[0] : "";
	String arg = (len > 1) ? parts[1] : "";

	try {
	    switch (cmd) {
	    case USBCON_VENDOR_SWITCH_TO_ACCESSORY:
		System.out.println("Preparing to connect to Vendor USB");
		try {
		    short tab3_vendorid = (short) 0x04e8; // Tab3,
		    usbController.setupUsb(tab3_vendorid);
		    int result = usbController.androidDeviceToAccessoryMode("CsohManufacturer", "CsohModel", "CsohDescription", "1.0", "http://www.mycompany.com", "SerialNumber");
		} catch (Exception e) {
		    broadCast(e.toString());
		    System.out.println(e);
		}
		break;

	    case USBCON_ACCESSORY:
		System.out.println("Connect to usb accessory");
		try {
		    usbController.setupUsbForAndroid();
		    Thread t = new Thread(usbController);
		    t.start();
		} catch (Exception e) {
		    broadCast(e.toString());
		    System.out.println(e);
		    ;
		}
		break;

	    case SEND_USB:
		usbController.sendMessageToAndroid(arg);
		break;
	    default:
		System.out.println("Unrecognized command '" + message + "'");
		break;

	    }
	} catch (Exception e) {
	    System.out.println(e);
	    broadCast(e.toString());

	}

    }

    private void broadCast(String message) {
	for (Session s : sessions.values()) {

	    try {
		s.getBasicRemote().sendText(message);
	    } catch (Exception e) {
		System.out.println("Error broadcasting messages to ws clients");
		System.out.println(e);
	    }
	}
    }

    public void startServer(String url, int port, String path) {
	server = new Server(url, port, path, WebsocketServerEndpoint.class);
	try {
	    server.start();
	} catch (DeploymentException e) {
	    e.printStackTrace();
	}
    }

    public void stopServer() {
	if (server != null)
	    server.stop();
    }

    public static void main(String[] args) {
	WebsocketServerEndpoint ws = new WebsocketServerEndpoint();
	try {
	    ws.startServer("localhost", 8025, "/websocket");
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    System.out.print("Please press a key to stop the server.\n");
	    reader.readLine();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    ws.stopServer();
	}
    }

    @Override
    public void onMessageFromUsb(String message) {
	broadCast(message);
    }

}
