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
app.constant("PAYLOAD",{
	TYPE : {
		KEY : "type",
		OUT : {
			USB: "USB",
		},
		IN : {
			DEVICE_LIST : "DEVICE_LIST",
			MESS : "MESS"
		}
	},
	DATA : {
		KEY : "data",
		USB_OPERATION : {
			KEY :{
				OPERATION_NAME : "operationName",
				VENDOR_NAME : "vendorName",
				VENDOR_ID : "vendorId",
				MESSAGE 	: "message"
			},
			USBCON_VENDOR_SWITCH_TO_ACCESSORY : "USBCON_VENDOR_SWITCH_TO_ACCESSORY",
			USBCON_ACCESSORY : "USBCON_ACCESSORY",
			SEND_USB : "SEND_USB",
			PING : "PING"
		}	
	}
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