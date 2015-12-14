app.controller('SuperController', function($scope, WebSocketClient,COMMAND,PAYLOAD_KEY) {
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
		payload[PAYLOAD_KEY.TYPE] = "USB";
		payload[PAYLOAD_KEY.DATA] = cmdData;
		WebSocketClient.sendMessage(JSON.stringify(payload));
	};
	$scope.sendSwitchToAccessory = function() {
		console.log($scope.selected);
		var selected = $scope.selectedUsbDevice;
		var payload = { "operationName" : COMMAND.USBCON_VENDOR_SWITCH_TO_ACCESSORY , "vendorId":selected.VENDOR_ID};
		$scope._sendCommand(payload);
		
	};
	$scope.sendUsbMessage = function(){
		console.log($scope.message);
		//var payload = { "operationName" : COMMAND.SEND_USB, "message" : $scope.message };
		//$scope._sendCommand(payload);
	};
	$scope.sendConnectAndroidUSB = function() {
		var payload = { "operationName" : COMMAND.USBCON_ACCESSORY };
		$scope._sendCommand(payload);
	};
	$scope.ping = function(arg) {
		$scope._sendCommand(COMMAND.PING +":" + arg)
	};
	
	$scope.onMessage = function(message){
		
		var data = undefined;
		try{
			data = JSON.parse(message);
		}catch(err){
			data ={};
			data[PAYLOAD_KEY.TYPE] = "MESS";
			data[PAYLOAD_KEY.DATA] = "ErrorParsing :" + message;
		}
		console.log(data[PAYLOAD_KEY.TYPE]);
		switch( data[PAYLOAD_KEY.TYPE] ){
			case "DEVICE_LIST" :
				$scope.usbDevices = [];
				for(var i in data[PAYLOAD_KEY.DATA]){
					var arec = data[PAYLOAD_KEY.DATA][i];
					$scope.usbDevices.push(arec);
				}
				break;
			case "MESS" : 
				$scope.appendStatus(data[PAYLOAD_KEY.DATA]);
			default :
					break;
		}
		$scope.$apply();
		
		
	};

});
