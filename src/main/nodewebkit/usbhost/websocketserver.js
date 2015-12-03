
app.factory("WebsocketServer", function(){
    var fac ={};
    fac.init = function(superController){
        var WebSocketServer = require("websocketserver");
        var server = new WebSocketServer("none", 9000);

        var connectionList = [];
        server.on("connection", function(id) {
            connectionList.push(id);
            console.log("Connection from :" + id);
        });

        server.on("message", function(data, id) {
            var mes = server.unmaskMessage(data);
            var str = server.convertToString(mes.message);
            switch(str){
        	case "INC" :
        		console.log("It's inc");	
                superController.incrementCount();
        		break;
        	default:
        		break;

            }

            console.log(str);
        });

        server.on("closedconnection", function(id) {
            connectionList.pop(id);
            console.log("Connection " + id + " has left the server");
            console.log("Remaining connections ");
            for(var i in connectionList){
        	console.log(i +") " +connectionList[i]);
            }
        });

        //server.on('connection', function(id) {
        //   server.sendMessage("one", "Welcome to the server!", id);
        //});

   
    }

    return fac;

    
});


