package csoh.app.usbcontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ResourceManager {

	private HashMap<Integer, HashMap<Integer, String>> lookupVendorProductName = null;

	private static ResourceManager instance = null;
	private static final int VENDOR_NAME_KEY = -99;
	
	public static ResourceManager getInstance() {
		if (instance == null) {
			synchronized (ResourceManager.class) {
				if (instance == null) {
					instance = new ResourceManager();
				}
			}
		}
		return instance;
	}

	public String getVendorName(int vendorId) {
		HashMap<Integer, String> vendorRec = lookupVendorProductName
				.get(vendorId);
		if (vendorRec == null)
			return null;
		return vendorRec.get(VENDOR_NAME_KEY);
	}

	public String getProductName(int vendorId, int productId) {
		HashMap<Integer, String> vendorRec = lookupVendorProductName
				.get(vendorId);
		if (vendorRec == null)
			return null;
		return vendorRec.get(productId);
	}

	private ResourceManager() {
		lookupVendorProductName = initVendorIdToName();
	}
	
	private HashMap<Integer, HashMap<Integer, String>> initVendorIdToName() {
		HashMap<Integer, HashMap<Integer, String>> prop = new HashMap<Integer, HashMap<Integer, String>>();
		InputStream input = null;
		String filename = "usb.ids.properties";
		try {
			input = UsbObject.class.getClassLoader().getResourceAsStream(
					filename);
			if (input != null) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(input, "UTF-8"));
				String aline = null;
				HashMap<Integer, String> currentProdIdToVendorName = null ; 
				while ((aline = reader.readLine()) != null) {
					if (aline.startsWith("#") || aline.equals(""))
						continue;
					if (! aline.startsWith("\t")) { // VendorID lines that doesn't start with tab
						String[] parts = aline.split("\\s+", 2);
						if (parts.length != 2)	continue;
						String sVendorId = parts[0];
						String sVendorName = parts[1];
						HashMap<Integer, String> prodIdToVendorName = new HashMap<Integer, String>();
						try {
							int vendorId = Integer.valueOf(sVendorId, 16);
							prodIdToVendorName.put(VENDOR_NAME_KEY, sVendorName);
							prop.put(vendorId, prodIdToVendorName);
							currentProdIdToVendorName = prodIdToVendorName;
						} catch (Exception e) {
							// System.out.println(e + " for " + vendorName);
							currentProdIdToVendorName = null;
						}
					}else{
						
						if (aline.matches("^\\t[^\\t].*")){ // ProductID lines that starts with a single tab
							String productLine = aline.trim();
							String[] parts = productLine.split("\\s+", 2);
							String sProductId = parts[0];
							String sProductName = parts[1];
							if(currentProdIdToVendorName != null){
								try {
									int productId = Integer.valueOf(sProductId, 16);
									currentProdIdToVendorName.put(productId, sProductName);
								} catch (Exception e) {
								}
							}
						}
					} 
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}
	
	public static void main(String[] args) {
		System.out.println(ResourceManager.getInstance().getVendorName(0x18D1));
		System.out.println(ResourceManager.getInstance().getProductName(0x18D1, 0x2D01));
		System.out.println(ResourceManager.getInstance().getVendorName(0x04e8));
	}

}
