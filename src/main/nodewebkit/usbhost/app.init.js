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

app.constant("IMAGE", {
	SWITCH_TO_ACCESSORY_INIT : "switchtoaccessory_init.jpg",
	SWITCH_TO_ACCESSORY_PASS : "switchtoaccessory_pass.jpg",
	SWITCH_TO_ACCESSORY_FAIL : "switchtoaccessory_fail.jpg",
	CONNECT_TO_ANDROID_INIT : "connecttoaccessory_init.jpg",
	CONNECT_TO_ANDROID_PASS : "connecttoaccessory_pass.jpg",
	CONNECT_TO_ANDROID_FAIL : "connecttoaccessory_fail.jpg"
});

app.constant("PAYLOAD",{
	TYPE : {
		KEY : "type",
		OUT : {
			USB: "USB",
		},
		IN : {
			DEVICE_LIST : "DEVICE_LIST",
			MESS : "MESS",
			STATUS : "STATUS"
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
		},
		STATUS :{
			KEY : { 
				TARGET : "target",
				STATE 	: "state",
				VALUE : "value"
			},
			SWITCH_TO_ACCESSORY : "SWITCH_TO_ACCESSORY",
			CONNECT_TO_ANDROID : "CONNECT_TO_ANDROID",
			PASS : "PASS",
			FAIL : "FAIL"
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