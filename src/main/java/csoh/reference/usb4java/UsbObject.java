package csoh.reference.usb4java;

import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class UsbObject {

    private int interfaceNum = 1;
    private Device device;
    private DeviceHandle handle;
    public DeviceHandle getHandle() {
        return handle;
    }
    private static final short PRODUCT_ID_DONT_CARE = -1;
    public UsbObject(short vendorId) throws DeviceNotFoundException {
	this(vendorId,PRODUCT_ID_DONT_CARE );
    }
    public UsbObject(short vendorId, short productId) throws DeviceNotFoundException{
	device = findDevice(vendorId, productId);
	if(device == null) throw new DeviceNotFoundException("Unable to find device for vendorId=" + vendorId +" and productId=" + productId);
	handle = new DeviceHandle();

	int result = LibUsb.open(device, handle);
	if (result != LibUsb.SUCCESS)  throw new LibUsbException("Unable to open USB device", result);

	detachKernelDriverIfKernelDriverActive();
	result = LibUsb.claimInterface(handle, interfaceNum);
	if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);
	
	
    }

    public void close() {
	LibUsb.attachKernelDriver(handle, interfaceNum);
	LibUsb.releaseInterface(handle, interfaceNum);
	LibUsb.close(handle);
    }

    private void detachKernelDriverIfKernelDriverActive() {
	if (LibUsb.kernelDriverActive(handle, interfaceNum) == 1) {
	    int r = LibUsb.detachKernelDriver(handle, interfaceNum);
	    if (r != LibUsb.SUCCESS && r != LibUsb.ERROR_NOT_SUPPORTED && r != LibUsb.ERROR_NOT_FOUND) {
		throw new LibUsbException("Unable to detach kernel driver", r);
	    }
	}
	// int supported = LibUsb.setAutoDetachKernelDriver(handle,true);
	// if(supported != LibUsb.SUCCESS){
	// throw new
	// LibUsbException("Unable to setAutoDetachedKernelDriver because it is not supported",supported);
	// }
    }
    private static Device findDevice(short vendorId) {
	return findDevice(vendorId, PRODUCT_ID_DONT_CARE);
    }
    private static Device findDevice(short vendorId, short productId) {
	// Read the USB device list
	DeviceList list = new DeviceList();
	int result = LibUsb.getDeviceList(null, list);
	if (result < 0) throw new LibUsbException("Unable to get device list", result);

	try {
	    // Iterate over all devices and scan for the right one
	    for (Device device : list) {
		DeviceDescriptor descriptor = new DeviceDescriptor();
		result = LibUsb.getDeviceDescriptor(device, descriptor);
		if (result != LibUsb.SUCCESS)    throw new LibUsbException("Unable to read device descriptor", result);
		if(productId == PRODUCT_ID_DONT_CARE){
		    if (descriptor.idVendor() == vendorId){
			 return device;
		    }
		}else{
		    if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId){
		    	return device;
		    }
		}
	    }
	} finally {
	    // Ensure the allocated device list is freed
	    LibUsb.freeDeviceList(list, true);
	}
	// Device not found
	return null;
    }

}
