package csoh.reference.usb4java;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbEndpoint;
import javax.usb.UsbEndpointDescriptor;
import javax.usb.UsbInterface;

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


public class AccessoryTest {

    private final static byte REQUEST_TYPE_READ = (byte) 0xC0;// 1100 0000 == USB_DIR_IN | USB_TYPE_VENDOR

    private static short VENDOR_ID = (short) 0x04e8; // Tab3, 
    private static short PRODUCT_ID = (short) 0x6860; // Tab3, 
    private static short VENDOR_ID_GOOGLE = (short)0x18D1; //google vendorid
    private static short PRODUCT_ID_GOOGLE = (short)0x2D01 ; //google productid
    private static byte END_POINT_IN_GOOGLE = (byte)0x81; // for interface == 1
    private static byte END_POINT_OUT_GOOGLE = (byte)0x02; // for interface == 1
    
//    private static int interfaceNum = 1;
//    
//    private static Device device;
//    private static DeviceHandle handle;
//    
    public static void main(String[] args) {
   	Context context=null;
   	try {
   	    context = init();
   	    /************** Switch Android device to Accessory mode *********/
   	    UsbObject usbForSetup = new UsbObject(VENDOR_ID);
   	    int result = androidDeviceToAccessoryMode(usbForSetup, "CsohManufacturer", "CsohModel",
		    "CsohDescription", "1.0", "http://www.mycompany.com",
		    "SerialNumber"); // Switch android device to accessory mode
   	    usbForSetup.close();
   	    
	    System.out.println("Finished switching android device to accessory mode, Sleeping 10 sec");
	    Thread.sleep(10000);
	    /************** Begin communication *********/
   	    UsbObject usbForCommunication =  new UsbObject(VENDOR_ID_GOOGLE);
   	    transferBulkData(usbForCommunication.getHandle(), END_POINT_OUT_GOOGLE , 100000);
   	 System.out.println("Transffered, Sleeping 1000 sec");
   	    Thread.sleep(1000000);
   	    
   	    usbForCommunication.close();
   	    System.out.println("Fin");
   	    
   	}catch(Exception e){
   	    e.printStackTrace();
   	}finally{
   	    LibUsb.exit(context);
   	}
       }

    private static Context init() {
	Context context = new Context();
	int result = LibUsb.init(context);
	if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);
	return context;
    }
  
    private static int androidDeviceToAccessoryMode(UsbObject usbObject, String vendor, String model, String description, String version, String url, String serial)
	    throws LibUsbException {
	
	int response = 0;
	DeviceHandle handle = usbObject.getHandle();
	    
	// Setup setup token
	response = transferSetupPacket(handle,REQUEST_TYPE_READ, (byte) 51);

	System.out.println("After setup token");
	// Setup data packet
	response = transferAccessoryDataPacket(handle,vendor, (short) 0);
	response = transferAccessoryDataPacket(handle,model, (short) 1);
	response = transferAccessoryDataPacket(handle,description, (short) 2);
	response = transferAccessoryDataPacket(handle,version, (short) 3);
	response = transferAccessoryDataPacket(handle,url, (short) 4);
	response = transferAccessoryDataPacket(handle,serial, (short) 5);
	System.out.println("After data packet");
	

	// Setup handshake packet
	response = transferSetupPacket(handle, LibUsb.REQUEST_TYPE_VENDOR, (byte) 53);

	System.out.println("After handshake packet");
	
	//LibUsb.releaseInterface(handle, interfaceNum);
	//System.out.println("After release Interface");
	return response;
    }

    private static int transferSetupPacket(DeviceHandle handle, byte requestType, byte request) throws LibUsbException {
	int response = 0;
	byte[] bytebuff = new byte[2];
	ByteBuffer data = BufferUtils.allocateByteBuffer(bytebuff.length);
	data.put(bytebuff);
	


	final short wValue = 0;
	final short wIndex = 0;
	final long timeout = 0;
	//data.rewind();
	//System.out.println("DAta_A=" + data.getShort());
	response = LibUsb.controlTransfer(handle, requestType, request, wValue,	wIndex, data, timeout);
	System.out.println("Response=" + response);
	//data.rewind();
	//System.out.println("DAta_B=" + data.getShort());
//	if (response < 0) throw new LibUsbException("Unable to transfer setup packet ", response);

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
	response = LibUsb.controlTransfer(handle, LibUsb.REQUEST_TYPE_VENDOR,bRequest, wValue, index, data, timeout);
	if (response < 0) throw new LibUsbException("Unable to control transfer.", response);
	return response;
    }
    private static int transferBulkData(DeviceHandle handle, byte endpoint, long timeout){
	ByteBuffer buffer = ByteBuffer.allocateDirect(8);
	buffer.put(new byte[] { 65, 67, 68, 69, 70, 71, 72, 82 });
	IntBuffer transfered = IntBuffer.allocate(1);
	int result = LibUsb.bulkTransfer(handle, endpoint, buffer, transfered, timeout); 
	if (result != LibUsb.SUCCESS) throw new LibUsbException("Control transfer failed", result);
	System.out.println(transfered.get() + " bytes sent");
	return result;
    
    }

}