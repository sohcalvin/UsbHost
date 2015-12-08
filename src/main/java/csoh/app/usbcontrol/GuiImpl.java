package csoh.app.usbcontrol;

import java.net.URI;
import java.net.URISyntaxException;

public class GuiImpl implements Gui {
    private WebsocketClientEndpoint clientEndPoint = null; 
    public GuiImpl() {
	try {
	    // open websocket
	    clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:9000"));

	    // add listener
	    clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
		public void handleMessage(String message) {
		    System.out.println("recv:" + message);
		}
	    });
	   

	} catch (URISyntaxException ex) {
	    System.err.println("URISyntaxException exception: " + ex.getMessage());
	}
    }

    @Override
    public void setProgressLevel(int level) {
	 // send message to websocket
	    clientEndPoint.sendMessage("INC");

    }

    @Override
    public void setMessageFromClient(String mess) {
	// TODO Auto-generated method stub

    }

   
}
