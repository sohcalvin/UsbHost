package csoh.app.usbcontrol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbEndpoint;
import javax.usb.UsbEndpointDescriptor;
import javax.usb.UsbInterface;
import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;
import org.usb4java.BufferUtils;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.Interface;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class UsbController implements Runnable {

    private Context context = null;
    private UsbObject currentUsb = null;

    private final static byte REQUEST_TYPE_READ = (byte) 0xC0;// 1100 0000 == USB_DIR_IN | USB_TYPE_VENDOR
    private static short VENDOR_ID = (short) 0x04e8; // Tab3,
    private static short PRODUCT_ID = (short) 0x6860; // Tab3,
    private static short VENDOR_ID_GOOGLE = (short) 0x18D1; // google vendorid
    private static short PRODUCT_ID_GOOGLE = (short) 0x2D01; // google productid
    private static byte END_POINT_IN_GOOGLE = (byte) 0x81; // for interface == 1
    private static byte END_POINT_OUT_GOOGLE = (byte) 0x02; // for interface ==

    private static UsbController instance = null;

    public static UsbController getInstance() {
	if (instance == null) {
	    synchronized (UsbController.class) {
		if (instance == null) {
		    instance = new UsbController();
		}
	    }
	}
	return instance;
    }

    private UsbController() {
	init();
    }
    

    public void setupUsb(short vendor_id, short product_id) throws DeviceNotFoundException {
	currentUsb = new UsbObject(vendor_id, product_id);
    }

    public void setupUsb(short vendor_id) throws DeviceNotFoundException {
	currentUsb = new UsbObject(vendor_id);
    }

    public void setupUsbForAndroid() throws DeviceNotFoundException {
	setupUsb(VENDOR_ID_GOOGLE);
    }

    public void sendMessageToAndroid(String messageToSend) {
	transferBulkData(currentUsb.getHandle(), END_POINT_OUT_GOOGLE, messageToSend, 10000);
    }

    private void init() {
	context = new Context();
	int result = LibUsb.init(context);
	if (result != LibUsb.SUCCESS)
	    throw new LibUsbException("Unable to initialize libusb.", result);
    }

    void closeUsbObject() {
	currentUsb.close();
    }
    void resetDevice(){
	currentUsb.resetDevice();
    }
    void exit() {
	LibUsb.exit(context);
    }

    // private static int interfaceNum = 1;
    // private static Device device;
    // private static DeviceHandle handle;
    //

    public static void main(String[] args) {
	Server server = null;
	try {
	    server = new Server("localhost", 8025, "/websocket", WebsocketServerEndpoint.class);
	    server.start();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    System.out.print("Please press a key to stop the server.\n");
	    reader.readLine();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (server != null)
		server.stop();
	}
    }

    public int androidDeviceToAccessoryMode(String vendor, String model, String description, String version, String url, String serial) throws LibUsbException, GeneralException {

	UsbObject usbObject = currentUsb;
	int response = 0;
	DeviceHandle handle = usbObject.getHandle();

	// Setup setup token, check if supports AOA #51
	response = transferSetupPacket(handle, REQUEST_TYPE_READ, (byte) 51);
	if (response <= 0) {
	    throw new GeneralException("Setup get protocol failed. Error code = " + response);
	}

	System.out.println("After setup token");
	// Setup data packet, switching to Accessory mode #52
	response = transferAccessoryDataPacket(handle, vendor, (short) 0);
	response = transferAccessoryDataPacket(handle, model, (short) 1);
	response = transferAccessoryDataPacket(handle, description, (short) 2);
	response = transferAccessoryDataPacket(handle, version, (short) 3);
	response = transferAccessoryDataPacket(handle, url, (short) 4);
	response = transferAccessoryDataPacket(handle, serial, (short) 5);
	System.out.println("After data packet");

	// Setup handshake packet #53
	response = transferSetupPacket(handle, LibUsb.REQUEST_TYPE_VENDOR, (byte) 53);

	System.out.println("After handshake packet");

	// LibUsb.releaseInterface(handle, interfaceNum);
	// System.out.println("After release Interface");
	usbObject.close();
	return response;
    }

    private int transferSetupPacket(DeviceHandle handle, byte requestType, byte request) throws LibUsbException {
	int response = 0;
	byte[] bytebuff = new byte[2];
	ByteBuffer data = BufferUtils.allocateByteBuffer(bytebuff.length);
	data.put(bytebuff);

	final short wValue = 0;
	final short wIndex = 0;
	final long timeout = 0;
	// data.rewind();
	// System.out.println("DAta_A=" + data.getShort());
	response = LibUsb.controlTransfer(handle, requestType, request, wValue, wIndex, data, timeout);
	System.out.println("Response=" + response);
	// data.rewind();
	// System.out.println("DAta_B=" + data.getShort());
	// if (response < 0) throw new
	// LibUsbException("Unable to transfer setup packet ", response);

	return response;
    }

    private static int transferAccessoryDataPacket(DeviceHandle handle, String param, short index) {
	int response;
	byte[] byteArray = param.getBytes();
	ByteBuffer data = BufferUtils.allocateByteBuffer(byteArray.length);
	data.put(byteArray);
	final byte bRequest = (byte) 52;
	final short wValue = 0;
	final long timeout = 0;
	response = LibUsb.controlTransfer(handle, LibUsb.REQUEST_TYPE_VENDOR, bRequest, wValue, index, data, timeout);
	if (response < 0)
	    throw new LibUsbException("Unable to control transfer.", response);
	return response;
    }

    private static int transferBulkData(DeviceHandle handle, byte endpoint, String message, long timeout) {
	int len = message.length();
	ByteBuffer buffer = ByteBuffer.allocateDirect(len);
	buffer.put(message.getBytes());

	// ByteBuffer buffer = ByteBuffer.allocateDirect(8);
	// buffer.put(new byte[] { 65, 67, 68, 69, 70, 71, 72, 82 });
	IntBuffer transfered = IntBuffer.allocate(1);
	int result = LibUsb.bulkTransfer(handle, endpoint, buffer, transfered, timeout);
	if (result != LibUsb.SUCCESS)
	    throw new LibUsbException("Control transfer failed", result);
	System.out.println(transfered.get() + " bytes sent");
	return result;

    }

    private int readBulkData(DeviceHandle handle, byte endpoint, int dataLength, long timeout) {
	ByteBuffer buffer = ByteBuffer.allocateDirect(dataLength);
	IntBuffer transfered = IntBuffer.allocate(1);
	int result = LibUsb.bulkTransfer(handle, endpoint, buffer, transfered, timeout);
	if (result != LibUsb.SUCCESS)
	    throw new LibUsbException("Control transfer failed", result);

	byte[] placeholder = new byte[dataLength];
	buffer.get(placeholder, 0, dataLength);
	String usbInMessage = new String(placeholder);
	notifyUsbMessageListeners(usbInMessage);
	printOut(usbInMessage);
	return result;
    }

    ArrayList<UsbMessageListener> usbMessageListeners = new ArrayList<UsbMessageListener>();

    void addUsbMessageListener(UsbMessageListener umListener) {
	printOut("addUsbMessageListener ");
	usbMessageListeners.add(umListener);
    }

    private void notifyUsbMessageListeners(String message) {
	printOut("In notifyUsbMessageListeners(" + message + ")");
	printOut("Number of listeners = '" + usbMessageListeners.size() + "");
	for (UsbMessageListener u : usbMessageListeners) {
	    printOut("Listener hashCode = " + u.hashCode());
	    u.onMessageFromUsb(message);
	}
    }

    @Override
    public void run() {
	try {
	    while (true) {
		notifyUsbMessageListeners("Waiting to readBulkData from device");
		readBulkData(currentUsb.getHandle(), END_POINT_IN_GOOGLE, 32, 0);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    notifyUsbMessageListeners(e.toString());
	}

    }

    private void printOut(String mess) {
	System.out.println("UsbTestLow:" + this.hashCode() + ">" + mess);
    }

    public String listConnectedDevices() {
	ArrayList<HashMap<String, Object>> list = UsbObject.getConnectedDeviceProperties();
	StringBuffer buf = new StringBuffer();
	String[] keys = new String[] { UsbObject.PRODUCT, UsbObject.PRODUCT_ID, UsbObject.VENDOR_ID, UsbObject.MANUFACTURER, UsbObject.SERIAL_NUMBER ,UsbObject.DUMP};
	for (HashMap<String, Object> d : list) {
	    buf.append("{ ");
	    for (String k : keys) {
		buf.append(k + " : " + d.get(k) + ", ");
	    }
	    buf.append(" }\n");
	}
	return buf.toString();
    }

}