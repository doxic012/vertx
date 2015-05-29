var WebSocketEvents = {
	event : {},
	unbind : function(eventName) {
		delete this.event.eventName;
	},
	
	bind : function(eventName, callback) {
		if(callback != undefined && callback != null) {
			this.event.eventName = callback;
		}
	},
	
	trigger : function(eventName) {
		this.event.eventName && this.event.eventName();
	},
};

window.WebSocketEvents = WebSocketEvents;

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