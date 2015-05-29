package io.vertx.webchat.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class WebSocketMessage implements WebSocketFrame {

	private ByteBuf binaryData;

	private WebSocketMessageType messageEvent;

	private String messageData;

	private boolean isReply;

	public WebSocketMessage(String message, WebSocketMessageType messageType, boolean isReply) {
		this.messageEvent = messageType;
		this.messageData = message;
		this.isReply = isReply;
		System.out.println(this.toString());
		this.binaryData = Unpooled.copiedBuffer(this.toString(), CharsetUtil.UTF_8);
	}

	public WebSocketMessage(JsonObject message, WebSocketMessageType messageType, boolean isReply) {
		this(message.encode(), messageType, isReply);
	}

	public WebSocketMessage(JsonArray message, WebSocketMessageType messageType, boolean isReply) {
		this(message.encode(), messageType, isReply);
	}

	public enum WebSocketMessageType {
		GetUserData, 
		SendMessageToUser, 
		MessageRetrieved, 
		GetMessageHistory, 
		GetContactList, 
		AddContact, 
		RemoveContact,
		NotifyContact,
		UserOnlineStatus
	}

	public JsonObject toJson() {
		JsonObject message = new JsonObject().put("messageEvent", messageEvent).put("messageData", messageData).put("isReply", isReply);

		return message;
	}

	@Override
	public String toString() {
		return this.toJson().encode();
	}

	@Override
	public boolean isText() {
		return true;
	}

	@Override
	public boolean isBinary() {
		return false;
	}

	@Override
	public boolean isContinuation() {
		return false;
	}

	@Override
	public String textData() {
		return binaryData.toString(CharsetUtil.UTF_8);
	}

	@Override
	public Buffer binaryData() {
		return Buffer.buffer(binaryData);
	}

	@Override
	public boolean isFinal() {
		return true;
	}
}
