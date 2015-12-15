package csoh.app.usbcontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class WebsocketMessageMarshaller {
	private static final String PAYLOAD_TYPE_KEY = "type";
	private static final String PAYLOAD_TYPE_IN_USB = "USB";
	private static final String PAYLOAD_TYPE_OUT_MESS = "MESS";
	private static final String PAYLOAD_TYPE_OUT_DEVICE_LIST = "DEVICE_LIST";
	private static final String PAYLOAD_DATA_KEY = "data";
	

	private static WebsocketMessageMarshaller instance = null;

	private static ObjectMapper mapper = new ObjectMapper();

	public static WebsocketMessageMarshaller getInstance() {
		if (instance == null) {
			synchronized (WebsocketMessageMarshaller.class) {
				if (instance == null) {
					instance = new WebsocketMessageMarshaller();
				}
			}
		}
		return instance;
	}

	private WebsocketMessageMarshaller() {
	}

	public String deviceListToJson(
			ArrayList<HashMap<UsbObject.DESCRIPTOR_FIELD, Object>> list) {
		HashMap<String, Object> wrapper = new HashMap<String, Object>(2);
		wrapper.put(PAYLOAD_TYPE_KEY, PAYLOAD_TYPE_OUT_DEVICE_LIST);
		wrapper.put(PAYLOAD_DATA_KEY, list);

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
		return "{\"" + PAYLOAD_TYPE_KEY + "\" : \""+PAYLOAD_TYPE_OUT_MESS +"\", \""
				+ PAYLOAD_DATA_KEY + "\" : \"" + mess + "\" }";
	}
	
	public Payload<UsbOperation> toPayloadUsbOperation(JsonNode node)
			throws JsonParseException, JsonMappingException, IOException {
		Payload<UsbOperation> p = mapper.convertValue(node,
				new TypeReference<Payload<UsbOperation>>() {
				});
		return p;
	}

	public JsonNode toJsonNode(String jsonString) throws JsonParseException,
			JsonMappingException, IOException {
		JsonNode n = mapper.readValue(jsonString, JsonNode.class);
		return n;
	}

	public boolean isUsbCommand(JsonNode node) {
		String type = node.get(WebsocketMessageMarshaller.PAYLOAD_TYPE_KEY)
				.asText();
		if (type.equals(PAYLOAD_TYPE_IN_USB))
			return true;
		return false;
	}

	public static class Payload<T> {
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

	public static class UsbOperation {
		private String operationName = null;
		private Short vendorId = null;
		private Short productId = null;
		private String message = null;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getOperationName() {
			return operationName;
		}

		public void setOperationName(String operationName) {
			this.operationName = operationName;
		}

		public Short getVendorId() {
			return vendorId;
		}

		public void setVendorId(Short vendorId) {
			this.vendorId = vendorId;
		}

		public Short getProductId() {
			return productId;
		}

		public void setProductId(Short productId) {
			this.productId = productId;
		}
	}

	public static void main(String[] args) { // Test main
		try {
			String data = "{\"type\" : \"CMD\", \"data\":{\"operationName\":\"AAA\"}}";
			JsonNode n = getInstance().toJsonNode(data);
			System.out.println(n.get("type"));

			Payload<UsbOperation> p = getInstance().toPayloadUsbOperation(n);

			UsbOperation o = p.getData();
			System.out.println(o.getOperationName());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
