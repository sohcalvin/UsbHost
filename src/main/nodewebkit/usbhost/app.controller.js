app.controller('SuperController', function($scope, WebSocketClient,PAYLOAD) {
	WebSocketClient.init($scope);
	$scope.status = "";
	$scope.count = 1;
	$scope.usbDevices = [];
	$scope.selectedUsbDevice;
	$scope.message = "";
	$scope.setCount = function(cnt) {
		$scope.count = cnt;
	};
	$scope.incrementCount = function() {
		$scope.count = $scope.count + 1;
		$scope.$apply();
	};
	$scope.appendStatus = function(message) {
		$scope.status += message + "\n";
		$scope.$apply();
	};
	
	$scope._sendCommand = function(cmdData){
		var payload = {};
		payload[PAYLOAD.TYPE.KEY] = PAYLOAD.TYPE.OUT.USB;
		payload[PAYLOAD.DATA.KEY] = cmdData;
		WebSocketClient.sendMessage(JSON.stringify(payload));
	};
	$scope.sendSwitchToAccessory = function() {
		console.log($scope.selected);
		var selected = $scope.selectedUsbDevice;
		if(! selected ){
			alert('Please select a device for connection');
//			BootstrapDialog.show({
//	            message: 'Please select a device for connection'
//	        });
			return;
		}
		var payload ={};
		payload[PAYLOAD.DATA.USB_OPERATION.KEY.OPERATION_NAME] = PAYLOAD.DATA.USB_OPERATION.USBCON_VENDOR_SWITCH_TO_ACCESSORY;
		payload[PAYLOAD.DATA.USB_OPERATION.KEY.VENDOR_ID] = selected.VENDOR_ID;
		$scope._sendCommand(payload);
		
	};
	$scope.sendUsbMessage = function(){
		var payload ={};
		payload[PAYLOAD.DATA.USB_OPERATION.KEY.OPERATION_NAME] = PAYLOAD.DATA.USB_OPERATION.SEND_USB;
		payload[PAYLOAD.DATA.USB_OPERATION.KEY.MESSAGE] =  $scope.message;
		$scope._sendCommand(payload);
	};
	$scope.sendConnectAndroidUSB = function() {
		var payload ={};
		payload[PAYLOAD.DATA.USB_OPERATION.KEY.OPERATION_NAME] = PAYLOAD.DATA.USB_OPERATION.USBCON_ACCESSORY;
		$scope._sendCommand(payload);
	};
	$scope.ping = function(arg) {
		var payload ={};
		payload[PAYLOAD.DATA.USB_OPERATION.KEY.OPERATION_NAME] =  PAYLOAD.DATA.USB_OPERATION.PING ;
		$scope._sendCommand(payload);
	};
	
	$scope.onMessage = function(message){
		
		var data = undefined;
		try{
			
			data = JSON.parse(message);
		}catch(err){
			console.log( message );
			console.log(err);
			data ={};
			data[PAYLOAD.TYPE.KEY] = PAYLOAD.TYPE.IN.MESS;
			data[PAYLOAD.DATA.KEY] = "ErrorParsing :" + message;
		}
		//console.log(data[PAYLOAD.TYPE.KEY]);
		switch( data[PAYLOAD.TYPE.KEY] ){
			case PAYLOAD.TYPE.IN.DEVICE_LIST :
				$scope.usbDevices = [];
				for(var i in data[PAYLOAD.DATA.KEY]){
					var arec = data[PAYLOAD.DATA.KEY][i];
					$scope.usbDevices.push(arec);
				}
				break;
			case PAYLOAD.TYPE.IN.MESS : 
				$scope.appendStatus(data[PAYLOAD.DATA.KEY]);
			default :
					break;
		}
		$scope.$apply();
	
	};

});
