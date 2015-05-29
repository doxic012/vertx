function WebSocketEvents() {
	var events = {};
	var self = this;
	
	// unbind an event
	self.unbind = function(eventName) {
		delete events[eventName];
	};
	
	// bind an event
	self.bind = function(eventName, callback) {
		if(callback != undefined && callback != null) {
			console.log("binding event "+eventName);
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
	WebSocketEvents.bind("GetContactList", function(event) {
		console.log("get contact list");
		console.log(event);
	});
	
	WebSocketEvents.bind("GetUserData", function(event) {
		console.log("get user data");
		console.log(event);
	});

	WebSocketEvents.bind("SendMessageToUser", function(message) {
		
	});
	WebSocketEvents.bind("MessageRetrieved", function(status) {
		
	});
	WebSocketEvents.bind("GetMessageHistory", function(history) {
		
	});
	WebSocketEvents.bind("AddContact", function(status) {
		
	});
	WebSocketEvents.bind("RemoveContact", function(status) {
		
	});
	WebSocketEvents.bind("NotifyContact", function(status) {
		
	});
	WebSocketEvents.bind("UserOnline", function(user) {
		console.log("user online");
		console.log(user);
	});
	WebSocketEvents.bind("UserOffline", function(user) {
		console.log("user offline");
		console.log(user);
	});
})();