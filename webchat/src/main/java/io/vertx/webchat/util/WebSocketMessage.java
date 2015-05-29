package io.vertx.webchat.util;

import io.vertx.core.json.JsonObject;

public class WebSocketMessage {
	public enum WebSocketEventType {
		
	}
	
	public WebSocketEventType messageEvent;
	
	public JsonObject data;
}
