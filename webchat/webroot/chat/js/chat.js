$(document).ready(function() {

	var eb = new vertx.EventBus("/eventbus/");
	var eb = new SockJS("/eventbus/");
	// Messages to client
	eb.onopen = function() {
		console.log("open");
		
//		eb.publish("chat.connection.open", {"connectionId" : 'testId'});
//		eb.registerHandler("chat.message.toClient", function(msg) {
//			console.log(msg);
//			$('.websocket-test-output').append(msg + "\n");
//		});
	};
	
	eb.onclose = function() {
		console.log("close");
//		eb.publish("chat.connection.close", {"connectionId" : 'testId'});
	}
	
	eb.onmessage = function(msg) {
		console.log("message");
		console.log(msg);
	}

	// if (window.WebSocket) {
	// socket = new WebSocket("ws://localhost:8080/chat");
	// socket.onmessage = function(event) {
	// console.log("Received data from websocket: ");
	// console.log(event.data);
	// $('.websocket-test-output').append(event.data + "\n");
	// }
	// socket.onopen = function(event) {
	// console.log("Web Socket opened!");
	// };
	// socket.onclose = function(event) {
	// console.log("Web Socket closed.");
	// };
	// } else {
	// console.log("Your browser does not support Websockets. (Use Chrome)");
	// }

	// function send(message) {
	// if (!window.WebSocket) {
	// return;
	// }
	// if (socket.readyState == WebSocket.OPEN) {
	// socket.send(message);
	// } else {
	// console.log("The socket is not open.");
	// }
	// }

	function send(message) {
		var input = $(".websocket-test-input").val();
		console.log("input value: " + input);
//		eb.publish("chat.message.toServer", message);
		eb.send(message);
	}

	var btn = $(".websocket-test-send").click(function() {
		var input = $(".websocket-test-input").val();
		console.log("input value: " + input);
		send(input);
	});
});
