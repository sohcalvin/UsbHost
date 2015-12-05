var app = angular.module('SuperApp', []);
app.constant("CONFIG", {
	XSERVER_ENDPOINT : "ws://berry:8025/websocket/usbhost",
	SERVER_ENDPOINT : "ws://localhost:8025/websocket/usbhost"
		
});

app.constant("WS_READY_STATE", {
	0 : "Connection not established",
	1 : "Connection established",
	2 : "Connection in closing handshake",
	3 : "Connection is closed or could not open"
});
app.constant("COMMAND",{
	USBCON_VENDOR_SWITCH_TO_ACCESSORY : "USBCON_VENDOR_SWITCH_TO_ACCESSORY",
	USBCON_ACCESSORY : "USBCON_ACCESSORY",
	SEND_USB : "SEND_USB"
	
});