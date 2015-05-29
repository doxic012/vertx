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

	WebSocketEvents.bind("SendMessageToUser", function(event) {
		
	});
	WebSocketEvents.bind("MessageRetrieved", function(event) {
		
	});
	WebSocketEvents.bind("GetMessageHistory", function(event) {
		
	});
	WebSocketEvents.bind("AddContact", function(event) {
		
	});
	WebSocketEvents.bind("RemoveContact", function(event) {
		
	});
	WebSocketEvents.bind("NotifyContact", function(event) {
		
	});
	WebSocketEvents.bind("UserOnlineStatus", function(event) {
		
	});

})();