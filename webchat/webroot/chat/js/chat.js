$(document).ready(function() {

	var eb = new vertx.EventBus("http://localhost:8080/chat");

	// Messages to client
	eb.onopen = function() {
		eb.registerHandler("webchat.msg.client", function(msg) {
			$('#websocket-test-output').append(msg + "\n");
		});
	};

//	function send(event) {
//		if (event.keyCode == 13 || event.which == 13) {
//			var message = $('#input').val();
//			if (message.length > 0) {
//				console.log($('#input'));
//				eb.publish("chat.to.server", message);
//				$('#input').val("");
//			}
//		}
//	}
	
	var btn = $(".websocket-test-send").click(function() {
		var input = $(".websocket-test-input").val();
		console.log("input value: " + input);
		eb.publish("webchat.msg.server", input);
	});
});
