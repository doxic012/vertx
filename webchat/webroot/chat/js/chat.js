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
 
function setActive(activeContact) {
	jQuery("div.contact-container").each(function() {
		jQuery(this).removeClass("active");
	});
	
	jQuery(activeContact).addClass("active");
};

angular.module('chatApp', []).controller('socketCtrl', function($scope) {
	$scope.contacts=[];

	$scope.getMessages = function(uid) {
		
	};
	
	if (window.WebSocket) {
		console.log("creating websocket");
		var socket = new ChatWebSocket("ws://localhost:8080/chat");

		socket.onopen = function(event) {
			console.log("Web Socket opened!");
		};

		socket.onclose = function(event) {
			console.log("Web Socket closed.");
		};

		socket.bind(socket.messageType.CONTACT_LIST, function(message) {
			$scope.$apply(function() {
				console.log("get contact list");
				console.log(message);
				$scope.contacts = message.messageData;
			});
		});

		socket.bind(socket.messageType.USER_DATA, function(message) {
			console.log("get user data");
			console.log(message);
		});

		socket.bind(socket.messageType.MESSAGE_SEND, function(message) {

		});
		socket.bind(socket.messageType.MESSAGE_READ, function(message) {

		});
		socket.bind(socket.messageType.MESSAGE_HISTORY, function(message) {

		});
		socket.bind(socket.messageType.CONTACT_ALL, function(message) {

		});
		socket.bind(socket.messageType.CONTACT_ADD, function(message) {
			
		});
		socket.bind(socket.messageType.CONTACT_REMOVE, function(message) {

		});
		socket.bind(socket.messageType.CONTACT_NOTIFY, function(message) {

		});
		socket.bind(socket.messageType.USER_ONLINE, function(message) {
			console.log("user online");
			console.log(message.messageData);
		});
		socket.bind(socket.messageType.USER_OFFLINE, function(message) {
			console.log("user offline");
			console.log(message.messageData);
		});
	}
});