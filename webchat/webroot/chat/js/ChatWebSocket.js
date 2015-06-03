function ChatWebSocket(url) {

	// no WebSockets supported
	if (!window.WebSocket) {
		console.log("Your browser does not support Websockets.(Use Chrome)");
		return null;
	}

	var socket = new WebSocket(url);

	// socket is undefined
	if (typeof socket === "undefined")
		return null;

	var events = {};

	// trigger an event and pass some data
	socket.trigger = function(eventName, data) {
		events[eventName] && events[eventName](data);
	};

	// unbind an event
	socket.unbind = function(eventName) {
		delete events[eventName];
	};

	// bind an event
	socket.bind = function(eventName, callback) {
		if (callback != undefined && callback != null) {
			console.log("binding event " + eventName);
			events[eventName] = callback;
		}
	};

	socket.onmessage = function(event) {
		var data = JSON.parse(event.data);

		if (socket.trigger)
			socket.trigger(data.messageType, data);
	}

	socket.messageType = {
		GetUserData : "GetUserData",
		SendMessage : "SendMessage",
		MessageRetrieved : "MessageRetrieved",
		GetMessageHistory : "GetMessageHistory",
		GetContactList : "GetContactList",
		AddContact : "AddContact",
		RemoveContact : "RemoveContact",
		NotifyContact : "NotifyContact",
		UserOnline : "UserOnline",
		UserOffline : "UserOffline"
	};

	// var defaultSend = socket.send; // We wrap send, so we need the original//
	// method
	socket.sendMessage = function(messageType, data, target, isReply) {
		var socketMsg = {
			messageType : messageType,
			messageData : data,
//			origin : "", // applied at server-side
			target : target,
			timestamp : new Date().toISOString(),
			reply : isReply
		}

		console.log(socketMsg);
		socket.send(JSON.stringify(socketMsg));
	};

	return socket;
}