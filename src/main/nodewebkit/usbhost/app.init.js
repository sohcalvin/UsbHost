var app = angular.module('SuperApp', []);
app.constant("CONFIG", {
	SERVER_ENDPOINT : "ws://berry:8025/websocket/usbhost",
	XSERVER_ENDPOINT : "ws://localhost:8025/websocket/usbhost"
		
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
app.directive('scrollToBottomOnUpdate', function() {
	  return {
	    restrict: 'A', //E = element, A = attribute, C = class, M = comment
	    link: function(scope, elem,attrs) {
	    	  scope.$watch(function () {
	              return elem[0].value;
	          },
	          function (e) {
	              elem[0].scrollTop = elem[0].scrollHeight;
	          });
	    }
	  }
	});