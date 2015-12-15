package csoh.app.usbcontrol;

import java.util.HashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.databind.JsonNode;

import csoh.app.usbcontrol.WebsocketMessageMarshaller.Payload;
import csoh.app.usbcontrol.WebsocketMessageMarshaller.UsbOperation;

@ServerEndpoint("/usbhost")
public class WebsocketServerEndpoint implements UsbMessageListener {
	private static final String USBCON_VENDOR_SWITCH_TO_ACCESSORY = "USBCON_VENDOR_SWITCH_TO_ACCESSORY";
	private static final String USBCON_ACCESSORY = "USBCON_ACCESSORY";
	private static final String SEND_USB = "SEND_USB";
	private static final String GET_SERVER_INFO = "GET_SERVER_INFO";
	private static final String GET_CONNECTED_DEVICES = "GET_CONNECTED_DEVICES";
	private static final String PING = "PING";

	private static final String LOGPREFIX = "WEBSOCKET >> ";

	UsbController usbController = UsbController.getInstance();
	WebsocketMessageMarshaller wsFactory = WebsocketMessageMarshaller.getInstance();

	private static HashMap<String, Session> sessions = new HashMap<String, Session>();

	@OnOpen
	public void open(Session session) {
		usbController.addUsbMessageListener(this);
		sessions.put(session.getId(), session);
		broadCast(usbController.listConnectedDevices());
		broadCast(wsFactory
				.messageToJson("Websocket session connected sessionId='"
						+ session.getId() + "'. Total sessions = "
						+ sessions.size()));
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

		try {
			JsonNode node = wsFactory.toJsonNode(message);
			//IHandler handler = HandlerFactory.getInstance().getHandler(node.get("type").asText());
			//if(handler != null)		handler.handle(this, usbController);
			System.out.println(node);

			if (wsFactory.isUsbCommand(node)) {
				Payload<UsbOperation> p = wsFactory.toPayloadUsbOperation(node);
				UsbOperation usbOper = p.getData();
				String oper = usbOper.getOperationName();

				switch (oper) {
				case USBCON_VENDOR_SWITCH_TO_ACCESSORY:
					try {
						short vendorid = usbOper.getVendorId(); // Tab3 is (short) 0x04e8
						usbController.setupUsb(vendorid);
						int result = usbController.androidDeviceToAccessoryMode("CsohManufacturer", "CsohModel",	"CsohDescription", "1.0", "http://www.mycompany.com","SerialNumber");
						broadCastInJson(USBCON_VENDOR_SWITCH_TO_ACCESSORY + " - done.");
						broadCastInJson("Please open Android application on device");
					} catch (Exception e) {
						broadCastInJson(e.toString());
						System.out.println(e);
					}
					break;

				case USBCON_ACCESSORY:
					try {
						usbController.setupUsbForAndroid();
						broadCastInJson(USBCON_ACCESSORY + " - done.");
						Thread t = new Thread(usbController);
						t.start();
					} catch (Exception e) {
						broadCastInJson(e.toString());
						System.out.println(e);
					}
					break;

				case SEND_USB:
					String arg = usbOper.getMessage();
					usbController.sendMessageToAndroid(arg);
					broadCast(SEND_USB + "(" + arg + ") - done.");
					break;
//				case GET_CONNECTED_DEVICES:
//					broadCast("GET_CONNECTED_DEVICES="
//							+ usbController.listConnectedDevices());
//					break;
				case PING:
				    broadCastInJson(usbController.listConnectedDevices());
					broadCastInJson("PINGED");
					break;
				case GET_SERVER_INFO:
					broadCast(getServerInfo());
					break;

				default:
					broadCast("Unrecognized command '" + message + "'");
					break;

				}
			}
		} catch (Exception e) {
			System.out.println(e);
			broadCast(e.toString());

		}

	}

	private void broadCastInJson(String message) {
		broadCast(message, true);
	}

	private void broadCast(String message) {
		broadCast(message, false);
	}

	private void broadCast(String message, boolean convertToJson) {
		if (convertToJson) {
			message = wsFactory.messageToJson(message);
		}
		for (Session s : sessions.values()) {
			try {
				// printOut("broadCast>>'" + message + "' to " + s.getId());
				s.getBasicRemote().sendText(message);
			} catch (Exception e) {
				printOut("Error broadcasting messages to ws clients");
				System.out.println(e);
			}
		}
	}

	private String getServerInfo() {
		StringBuffer buf = new StringBuffer();
		buf.append("Current sessions : ");
		for (Session s : sessions.values()) {
			buf.append(s.getId() + ";");
		}
		return buf.toString();
	}

	@Override
	public void onMessageFromUsb(String message) {
		broadCastInJson(message);
	}

	private void printOut(String mess) {
		System.out.println(LOGPREFIX + mess);
	}
}
