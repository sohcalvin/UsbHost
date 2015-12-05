
app.factory("WebSocketClient", function(WS_READY_STATE,CONFIG) {
	var fac = {};
	fac.superController = undefined;
	fac.ws = undefined;
	fac.init = function(superController) {
		this.superController = superController;
		if ("WebSocket" in window) {
			var ws = new WebSocket(CONFIG.SERVER_ENDPOINT);
			this.ws = ws;
			ws.onopen = function() {
				superController.appendStatus("Websocket connected");
			};

			ws.onmessage = function(evt) {
				var received_msg = evt.data;
				superController.appendStatus("Recv : " + received_msg);
				superController.incrementCount();
			};

			ws.onerror = function(evt) {
				superController.appendStatus("Error occured ");
			}
			ws.onclose = function(evt) {
				superController
						.appendStatus("Websocket connection closed. Code = "
								+ evt.code);
			};
		} else {
			alert("WebSocket NOT supported by your Browser!");
		}
	};
	fac.sendMessage = function(mess) {
		if (this.isConnectionReady()) {
			this.ws.send(mess);
		} else {
			this.superController.appendStatus(this.getConnectionStatus());
		}
	};
	fac.isConnectionReady = function() {
		return (this.ws.readyState === 1);
	};
	fac.getConnectionStatus = function() {
		return WS_READY_STATE[this.ws.readyState];
	};
	return fac;

});
