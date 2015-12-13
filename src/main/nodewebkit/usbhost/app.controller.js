app.controller('SuperController', function($scope, WebSocketClient,COMMAND,PAYLOAD_KEY) {
	WebSocketClient.init($scope);
	$scope.status = "";
	$scope.count = 1;
	$scope.usbDevices = [];
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
	$scope.sendMessage = function(){
		var payload = {};
		payload[PAYLOAD_KEY.TYPE] = "MESS";
		payload[PAYLOAD_KEY.DATA] = COMMAND.SEND_USB + ":"+$scope.message;
		WebSocketClient.sendMessage( JSON.stringify(payload));
		//WebSocketClient.sendMessage(COMMAND.SEND_USB + ":" +$scope.message);
	};
	$scope._sendCommand = function(cmdData){
		var payload = {};
		payload[PAYLOAD_KEY.TYPE] = "CMD";
		payload[PAYLOAD_KEY.DATA] = cmdData;
		WebSocketClient.sendMessage(JSON.stringify(payload));
	};
	$scope.sendSwitchToAccessory = function() {
		$scope._sendCommand(COMMAND.USBCON_VENDOR_SWITCH_TO_ACCESSORY);
		
	};
	$scope.sendConnectAndroidUSB = function() {
		$scope._sendCommand(COMMAND.USBCON_ACCESSORY);
		
	};
	$scope.ping = function(arg) {
		$scope._sendCommand(COMMAND.PING +":" + arg)
	};
	
	$scope.onMessage = function(message){
		var data = undefined;
		try{
			data = JSON.parse(message);
		}catch(err){
			data ={"TYPE":"MESS", "DATA": message};
		}

		switch( data.TYPE ){
			case "DEVICE_LIST" :
				$scope.usbDevices = [];
				for(var i in data.DATA){
					var arec = data.DATA[i];
					$scope.usbDevices.push(arec);
				}
				break;
			case "MESS" : 
				$scope.appendStatus(data.DATA);
			default :
					break;
		}
		$scope.$apply();
		
		
	};

});
