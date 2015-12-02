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
    private static final String CONNECT_USB_VENDOR = "CONNECT_USB_VENDOR";
    private static final String ANDROID_TO_ACCESSORY = "ANDROID_TO_ACCESSORY";
    private static final String CONNECT_USB_ACCESSORY = "CONNECT_USB_ACCESSORY";

    private static Server server = null;

    UsbTestLow usbController = UsbTestLow.getInstance();

    @OnOpen
    public void open(Session session) {
	System.out.println("Open session " + session);
	System.out.println("Websocket this> " + this);
    }

    @OnClose
    public void close(Session session) {
	System.out.println("Close session " + session);
	//t.join();
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
	try {
	    switch (message) {
	    case CONNECT_USB_VENDOR:
		System.out.println("Preparing to connect to Vendor USB");
		short tab3_vendorid = (short) 0x04e8; // Tab3,
		usbController.setupUsb(tab3_vendorid);
		break;
	    case ANDROID_TO_ACCESSORY:
		System.out.println("Switch android to accessory");
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

	    default:
		System.out.println("Unrecognized command '" + message + "'");
		break;

	    }
	} catch (DeviceNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (GeneralException ge) {
	    ge.printStackTrace();
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
