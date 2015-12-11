package csoh.app.usbcontrol;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;



public class WebsocketMessageFactory {
	private final String PROPERTY_ID = "ID";
	private final String PROPERTY_DATA = "DATA";
	private static WebsocketMessageFactory instance = null;

	public static WebsocketMessageFactory getInstance() {
		if (instance == null) {
			synchronized (WebsocketMessageFactory.class) {
				if (instance == null) {
					instance = new WebsocketMessageFactory();
				}
			}
		}
		return instance;
	}

	private WebsocketMessageFactory(){}
	
	public String deviceListToJson(ArrayList<HashMap<UsbObject.DESCRIPTOR_FIELD, Object>> list){
		HashMap<String, Object> wrapper = new HashMap<String,Object>(2);
		wrapper.put(PROPERTY_ID, "DEVICE_LIST");
		wrapper.put(PROPERTY_DATA, list);
		
		ObjectMapper mapper = new ObjectMapper();
	    mapper.enable(SerializationFeature.INDENT_OUTPUT); // print pretty
	    String json = null;
		try {
			json = mapper.writeValueAsString(wrapper);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	     return json;
		
	}
	
}
