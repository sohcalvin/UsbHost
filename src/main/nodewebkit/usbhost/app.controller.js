app.controller('SuperController', function($scope, WebSocketClient,COMMAND) {
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
		//var textarea = document.getElementById('comment');
		//textarea.scrollTop = textarea.scrollHeight;
	};
	$scope.sendMessage = function() {
		WebSocketClient.sendMessage(COMMAND.SEND_USB + ":" +$scope.message);
	};
	$scope.sendSwitchToAccessory = function() {
		WebSocketClient.sendMessage(COMMAND.USBCON_VENDOR_SWITCH_TO_ACCESSORY);
	};
	$scope.sendConnectAndroidUSB = function() {
		WebSocketClient.sendMessage(COMMAND.USBCON_ACCESSORY);
	};
	$scope.ping = function(arg) {
		WebSocketClient.sendMessage(COMMAND.PING +":" + arg);
	};
	$scope.onMessage = function(message){
		var data = JSON.parse(message);
		switch( data.ID ){
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
