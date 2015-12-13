package csoh.app.usbcontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class WebsocketMessageFactory {
    private final String PAYLOAD_KEY_TYPE = "TYPE";
    private final String PAYLOAD_KEY_DATA = "DATA";
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

    private WebsocketMessageFactory() {
    }

    public String deviceListToJson(ArrayList<HashMap<UsbObject.DESCRIPTOR_FIELD, Object>> list) {
	HashMap<String, Object> wrapper = new HashMap<String, Object>(2);
	wrapper.put(PAYLOAD_KEY_TYPE, "DEVICE_LIST");
	wrapper.put(PAYLOAD_KEY_DATA, list);

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

    public String messageToJson(String mess) {
	return "{\"" + PAYLOAD_KEY_TYPE + "\" : \"MESS\", \"" + PAYLOAD_KEY_DATA + "\" : \"" + mess + "\" }";
    }
    
    public Payload jsonToPayload(String jsonString) throws JsonParseException, JsonMappingException, IOException{
	
	ObjectMapper mapper = new ObjectMapper();
	Payload p = mapper.readValue(jsonString, new TypeReference<Payload<HashMap>>() {
	    });

	System.out.println(p.toString());
	
	return p;
    }

    public class Payload<T> {
	private String type = null;
	private T data = null;
	

	public String getType() {
	    return type;
	}
	public void setType(String type) {
	    this.type = type;
	}
	public T getData() {
	    return data;
	}
	public void setData(T data) {
	    this.data = data;
	}
	
	
    }

    public static void main(String[] args) {
	try {
	    String data = "{\"type\" : \"CMD\", \"data\"={}}";
	    ObjectMapper mapper = new ObjectMapper();
	  //  Map<String, Object> map = new HashMap<String, Object>();


	   // Map<String, Object> map = mapper.readValue(data, new TypeReference<Map<String, String>>() {});

	    Payload p = mapper.readValue(data, new TypeReference<Payload<HashMap<String,String>>>() {
	    });
	    System.out.println(p.getType());
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

}
