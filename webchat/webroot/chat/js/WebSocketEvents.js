var WebSocketMessageType = {
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

function WebSocketMessage(messageType, message, isReply) {
	var self = this;
	
	self.messageType = messageType;
	self.messageData = message;
	self.isReply = isReply;	
};

function WebSocketEvents() {
	var events = {};
	var self = this;

	// unbind an event
	self.unbind = function(eventName) {
		delete events[eventName];
	};

	// bind an event
	self.bind = function(eventName, callback) {
		if (callback != undefined && callback != null) {
			console.log("binding event " + eventName);
			events[eventName] = callback;
		}
	};

	// trigger an event and pass some data
	self.trigger = function(eventName, data) {
		events[eventName] && events[eventName](data);
	};
};

window.WebSocketEvents = new WebSocketEvents();

(function() {
	WebSocketEvents.bind(WebSocketMessageType.GetContactList, function(event) {
		console.log("get contact list");
		console.log(event);
	});

	WebSocketEvents.bind(WebSocketMessageType.GetUserData, function(event) {
		console.log("get user data");
		console.log(event);
	});

	WebSocketEvents.bind(WebSocketMessageType.SendMessage, function(message) {

	});
	WebSocketEvents.bind(WebSocketMessageType.MessageRetrieved, function(status) {

	});
	WebSocketEvents.bind(WebSocketMessageType.GetMessageHistory, function(history) {

	});
	WebSocketEvents.bind(WebSocketMessageType.AddContact, function(status) {

	});
	WebSocketEvents.bind(WebSocketMessageType.RemoveContact, function(status) {

	});
	WebSocketEvents.bind(WebSocketMessageType.NotifyContact, function(status) {

	});
	WebSocketEvents.bind(WebSocketMessageType.UserOnline, function(user) {
		console.log("user online");
		console.log(user);
	});
	WebSocketEvents.bind(WebSocketMessageType.UserOffline, function(user) {
		console.log("user offline");
		console.log(user);
	});
})();