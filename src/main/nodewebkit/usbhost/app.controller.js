

app.controller('SuperController', function($scope, WebSocketClient) {
	WebSocketClient.init($scope);
	$scope.status = "";
	$scope.count = 1;
    	$scope.message = "";
    	$scope.name = "John";
	$scope.getFullName = function() { return "Hello " + $scope.name; };
	$scope.setCount = function(cnt) {$scope.count = cnt; };
	$scope.incrementCount = function() {
		$scope.count = $scope.count + 1;
		$scope.$apply(); 
	};
	$scope.appendStatus = function(message){
		$scope.status += message + "\n";
		$scope.$apply(); 
	}
	$scope.sendMessage= function(){
		WebSocketClient.sendMessage($scope.message);
	}



});

