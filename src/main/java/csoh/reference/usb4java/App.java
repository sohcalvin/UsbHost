package csoh.reference.usb4java;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbPort;
import javax.usb.UsbServices;

/**
 * Hello world!
 *
 */
public class App {
    /** First init packet to send to the missile launcher. */
    private static final byte[] INIT_A = new byte[] { 85, 83, 66, 67, 0, 0, 4,
        0 };

    /** Second init packet to send to the missile launcher. */
    private static final byte[] INIT_B = new byte[] { 85, 83, 66, 67, 0, 64, 2,
        0 };
    /** Command to rotate the launcher up. */
    private static final int CMD_UP = 0x01;

    /** Command to rotate the launcher down. */
    private static final int CMD_DOWN = 0x02;

    /** Command to rotate the launcher to the left. */
    private static final int CMD_LEFT = 0x04;

    /** Command to rotate the launcher to the right. */
    private static final int CMD_RIGHT = 0x08;

    /** Command to fire a missile. */
    private static final int CMD_FIRE = 0x10;
    
    
    public static void main(String[] args) throws SecurityException, UsbException {
	System.out.println("Hello Usb!");
	final UsbServices services = UsbHostManager.getUsbServices();
	UsbHub rootHub = services.getRootUsbHub();
	// printDevices(rootHub);
	// dumpDevice(rootHub);
	UsbDevice device = findDevice(rootHub, (short) 0x04e8, (short) 0x6860); // tab3
	if (device != null) {
	    System.out.println(device);
	    // Claim the interface
	    UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
	    UsbInterface iface = configuration.getUsbInterface((byte) 1); 
	    
	    iface.claim(new UsbInterfacePolicy() {
		@Override 
		public boolean forceClaim(UsbInterface usbInterface) {
		    return true;
		}
	    });
	    
	    // Read commands and execute them
	    System.out.println("WADX = Move, S = Stop, F = Fire, Q = Exit");
	    boolean exit = false;
	    while (!exit) {
		System.out.print("> ");
		char key = readKey();
		switch (key) {
		case 'w':
		    sendCommand(device, CMD_UP);
		    break;

		case 'x':
		    sendCommand(device, CMD_DOWN);
		    break;

		case 'a':
		    sendCommand(device, CMD_LEFT);
		    break;

		case 'd':
		    sendCommand(device, CMD_RIGHT);
		    break;

		case 'f':
		    sendCommand(device, CMD_FIRE);
		    break;

		case 's':
		    sendCommand(device, 0);
		    break;

		case 'q':
		    exit = true;
		    break;

		default:
		}
	    }

	} else {
	    System.out.println("UsbDevice is null");

	}

    }
    /**
     * Read a key from stdin and returns it.
     * 
     * @return The read key.
     */
    public static char readKey() {
	try {
	    String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
	    if (line.length() > 0)
		return line.charAt(0);
	    return 0;
	} catch (IOException e) {
	    throw new RuntimeException("Unable to read key", e);
	}
    }
    public static void printDevices(UsbHub rootHub) {
	for (UsbDevice device : (List<UsbDevice>) rootHub.getAttachedUsbDevices()) {
	    UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
	    System.out.println(desc);

	}
    }

    public static UsbDevice findDevice(UsbHub hub, short vendorId,
	    short productId) {
	for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
	    UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
	    if (desc.idVendor() == vendorId && desc.idProduct() == productId)
		return device;
	    if (device.isUsbHub()) {
		device = findDevice((UsbHub) device, vendorId, productId);
		if (device != null)
		    return device;
	    }
	}
	return null;
    }

    private static void dumpDevice(final UsbDevice device) {
	// Dump information about the device itself
	System.out.println(device);
	final UsbPort port = device.getParentUsbPort();
	if (port != null) {
	    System.out.println("Connected to port: " + port.getPortNumber());
	    System.out.println("Parent: " + port.getUsbHub());
	}

	// Dump device descriptor
	System.out.println(device.getUsbDeviceDescriptor());

	// Process all configurations
	for (UsbConfiguration configuration : (List<UsbConfiguration>) device
		.getUsbConfigurations()) {
	    // Dump configuration descriptor
	    System.out.println(configuration.getUsbConfigurationDescriptor());

	    // Process all interfaces
	    for (UsbInterface iface : (List<UsbInterface>) configuration
		    .getUsbInterfaces()) {
		// Dump the interface descriptor
		System.out.println(iface.getUsbInterfaceDescriptor());

		// Process all endpoints
		for (UsbEndpoint endpoint : (List<UsbEndpoint>) iface
			.getUsbEndpoints()) {
		    // Dump the endpoint descriptor
		    System.out.println(endpoint.getUsbEndpointDescriptor());
		}
	    }
	}

	System.out.println();

	// Dump child devices if device is a hub
	if (device.isUsbHub()) {
	    final UsbHub hub = (UsbHub) device;
	    for (UsbDevice child : (List<UsbDevice>) hub
		    .getAttachedUsbDevices()) {
		dumpDevice(child);
	    }
	}
    }

    public static void sendCommand(UsbDevice device, int command)
	    throws UsbException {
	byte[] message = new byte[64];
	message[1] = (byte) ((command & CMD_LEFT) > 0 ? 1 : 0);
	message[2] = (byte) ((command & CMD_RIGHT) > 0 ? 1 : 0);
	message[3] = (byte) ((command & CMD_UP) > 0 ? 1 : 0);
	message[4] = (byte) ((command & CMD_DOWN) > 0 ? 1 : 0);
	message[5] = (byte) ((command & CMD_FIRE) > 0 ? 1 : 0);
	message[6] = 8;
	message[7] = 8;
	sendMessage(device, INIT_A);
	sendMessage(device, INIT_B);
	sendMessage(device, message);
    }

    public static void sendMessage(UsbDevice device, byte[] message)
	    throws UsbException {
	UsbControlIrp irp = device
		.createUsbControlIrp(
			(byte) (UsbConst.REQUESTTYPE_TYPE_CLASS | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE),
			(byte) 0x09, (short) 2, (short) 1);
	irp.setData(message);
	device.syncSubmit(irp);
    }

}
