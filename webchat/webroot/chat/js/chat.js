$(document).ready(
		function() {
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

			var btn = $("button[name=sendmessage]").click(
					function() {
						var input = $("textarea[name=message]").val();
						console.log("input value: " + input);
						socket.sendMessage(socket.messageType.SendMessage,
								input, "target", false);
					});
		});

angular.module('chatApp', []).controller('socketCtrl', function($scope) {
	$scope.contacts=[]; 
	if (window.WebSocket) {
		console.log("creating websocket");
		var socket = new ChatWebSocket("ws://localhost:8080/chat");

		socket.onopen = function(event) {
			console.log("Web Socket opened!");
		};

		socket.onclose = function(event) {
			console.log("Web Socket closed.");
		};

		socket.bind(socket.messageType.ContactList, function(message) {
			$scope.$apply(function() {
				console.log("get contact list");
				console.log(message);
				$scope.contacts = message.messageData;
			});
		});

		socket.bind(socket.messageType.UserData, function(message) {
			console.log("get user data");
			console.log(message);
		});

		socket.bind(socket.messageType.MessageSend, function(message) {

		});
		socket.bind(socket.messageType.MessageRead, function(message) {

		});
		socket.bind(socket.messageType.MessageHistory, function(message) {

		});
		socket.bind(socket.messageType.AddContact, function(message) {

		});
		socket.bind(socket.messageType.RemoveContact, function(message) {

		});
		socket.bind(socket.messageType.NotifyContact, function(message) {

		});
		socket.bind(socket.messageType.UserOnline, function(message) {
			console.log("user online");
			console.log(message.messageData);
		});
		socket.bind(socket.messageType.UserOffline, function(message) {
			console.log("user offline");
			console.log(message.messageData);
		});
	}
});