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
    private static final String GET_SERVER_INFO = "GET_SERVER_INFO";

    private static final String LOGPREFIX = "WEBSOCKET >> ";
    private Server server = null;

    UsbTestLow usbController = UsbTestLow.getInstance();

    private static HashMap<String, Session> sessions = new HashMap<String, Session>();

    private void doTest() {
	for (int i = 0; i < 100; i++)
	    broadCast("mess");
    }

    @OnOpen
    public void open(Session session) {
	sessions.put(session.getId(), session);
	broadCast("Websocket session connected sessionId='" + session.getId() + "'");
	broadCast(getServerInfo());
	// doTest();
    }

    @OnClose
    public void close(Session session) {
	broadCast("Close session " + session.getId());
	sessions.remove(session.getId());
	usbController.closeUsbObject();
	usbController.exit();
    }

    @OnError
    public void onError(Throwable error) {
	printOut("Error  " + error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
	printOut("Mess='" + message + "' Session='" + session.getId() + "'");
	String[] parts = message.split(":");
	int len = parts.length;
	String cmd = (len >= 1) ? parts[0] : "";
	String arg = (len > 1) ? parts[1] : "";

	printOut("Parsed cmd : '" + cmd + "'");

	try {
	    switch (cmd) {
	    case USBCON_VENDOR_SWITCH_TO_ACCESSORY:
		printOut("Preparing to connect to Vendor USB");
		try {
		    short tab3_vendorid = (short) 0x04e8; // Tab3,
		    usbController.setupUsb(tab3_vendorid);
		    int result = usbController.androidDeviceToAccessoryMode("CsohManufacturer", "CsohModel", "CsohDescription", "1.0", "http://www.mycompany.com", "SerialNumber");
		    broadCast(USBCON_VENDOR_SWITCH_TO_ACCESSORY + " - done.");
		    broadCast("Please open Android application on device");
		} catch (Exception e) {
		    broadCast(e.toString());
		    System.out.println(e);
		}
		break;

	    case USBCON_ACCESSORY:
		try {
		    usbController.setupUsbForAndroid();
		    broadCast(USBCON_ACCESSORY + " - done.");
		    Thread t = new Thread(usbController);
		    t.start();
		} catch (Exception e) {
		    broadCast(e.toString());
		    System.out.println(e);
		}
		break;

	    case SEND_USB:
		usbController.sendMessageToAndroid(arg);
		broadCast(SEND_USB +"(" +arg+") - done.");
		break;
	    case GET_SERVER_INFO:
		broadCast(getServerInfo());
		break;

	    default:
		printOut("Unrecognized command '" + message + "'");
		//	broadCast("Unrecognized command '" + message + "'");
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
		printOut("broadCast - '" + message + "' to " + s.getId());
		s.getBasicRemote().sendText("WS: " + message);
	    } catch (Exception e) {
		printOut("Error broadcasting messages to ws clients");
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

    private String getServerInfo() {
	StringBuffer buf = new StringBuffer();
	buf.append("Current sessions : ");
	for (Session s : sessions.values()) {
	    buf.append(s.getId() + ";");
	}
	return buf.toString();
    }

    public static void main(String[] args) {
	WebsocketServerEndpoint ws = new WebsocketServerEndpoint();
	UsbTestLow.getInstance().addUsbMessageListener(ws);
	try {
	    ws.startServer("localhost", 8025, "/websocket");
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    ws.printOut("Please press a key to stop the server.\n");
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

    private void printOut(String mess) {
	System.out.println(LOGPREFIX + mess);
    }
}
