
app.factory("WebSocketClient", function(){
    var fac ={};
    fac.ws = undefined;
    fac.init = function(superController){
         if ("WebSocket" in window){
               var ws = new WebSocket("ws://localhost:8025/websocket/usbhost");
		this.ws = ws;
               ws.onopen = function() {
                        superController.appendStatus("Websocket connected"); 
               };

               ws.onmessage = function (evt){
                  var received_msg = evt.data;
                  //superController.appendStatus("Recv : " + received_msg); 
                  superController.incrementCount(); 
               };

               ws.onerror = function(evt){
                  superController.appendStatus("Error occured "); 
	       }
               ws.onclose = function(evt){
                  superController.appendStatus("Websocket connection closed. Code = " + evt.code); 
               };
            }else{
               alert("WebSocket NOT supported by your Browser!");
            }
    }
    fac.sendMessage = function(mess){
                 	this.ws.send(mess);
    }
    return fac;

    
});


