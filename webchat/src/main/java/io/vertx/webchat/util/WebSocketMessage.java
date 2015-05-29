package io.vertx.webchat.util;

import io.vertx.core.json.JsonObject;

public class WebSocketMessage {
	public enum WebSocketEventType {
		SendMessageToUser,
		MessageRetrieved,
		GetMessageHistory,
		GetContactList,
		AddContact,
		RemoveContact,
		NotifyContact,
		UserOnlineStatus
	}
	
	private WebSocketEventType messageEvent;
	
	private String messageData;
	
	private boolean isReply;
	
	public JsonObject toJson() {
		JsonObject message = new JsonObject()
				.put("messageEvent", messageEvent)
				.put("messageData", messageData)
				.put("isReply", isReply);
		
		return message;
	}
}
