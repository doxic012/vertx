

$(document).ready(function() {
	if (window.WebSocket) {
		socket = new WebSocket("ws://localhost:8080/chat");

		socket.onmessage = function(event) {
			console.log("Received data from websocket: ");
			console.log(event.data);
			$('.message-box-container').append(event.data + "\n");
			
			if(window.WebSocketEvents)
			WebSocketEvents.trigger(event.messageEvent);
		}
		
		socket.onopen = function(event) {
			console.log("Web Socket opened!");
		};
		
		socket.onclose = function(event) {
			console.log("Web Socket closed.");
		};
	} else {
		console.log( "Your browser does not support Websockets.(Use Chrome)");
	}

	function send(message) {
		if (!window.WebSocket) {
			return;
		}
		if (socket.readyState == WebSocket.OPEN) {
			socket.send(message);
		} else {
			console.log("The socket is not open.");
		}
	}

	var btn = $("button[name=sendmessage]").click(function() {
		var input = $("textarea[name=message]").val();
		console.log("input value: " + input);
		eb.send(message);
	});
});
