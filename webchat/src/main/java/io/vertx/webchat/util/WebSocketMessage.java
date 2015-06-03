package io.vertx.webchat.util;

import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.impl.Json;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class WebSocketMessage {

	public enum WebSocketMessageType {
		GetUserData, SendMessage, MessageRetrieved, GetMessageHistory, GetContactList, AddContact, RemoveContact, NotifyContact, UserOnline, UserOffline
	}

	private String messageType;

	private Object messageData;

	private String origin = "server";

	private String target;

	private Timestamp timestamp;

	private boolean reply;

	@JsonCreator
	public WebSocketMessage() {

	}

	@JsonIgnore
	public WebSocketMessage(WebSocketMessageType messageType, Object messageData, String origin, String target, Timestamp timestamp, boolean isReply) {
		this.setMessageType(messageType.toString());
		this.setMessageData(messageData);
		this.setOrigin(origin);
		this.setTarget(target);
		this.setTimestamp(timestamp);
		this.setReply(isReply);

		System.out.println(this.toString());
	}

	@JsonIgnore
	public WebSocketMessage(WebSocketMessageType messageType, Object messageData) {
		this.setMessageType(messageType.toString());
		this.setMessageData(messageData);
		this.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		this.setReply(reply);

		System.out.println(this.toString());
	}

	public WebSocketFrame toFrame() {
		return WebSocketFrame.textFrame(this.toString(), true);
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Object getMessageData() {
		return messageData;
	}

	public void setMessageData(Object messageData) {
		this.messageData = messageData;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public boolean getReply() {
		return reply;
	}

	public void setReply(boolean isReply) {
		this.reply = isReply;
	}

	@Override
	public String toString() {
		return Json.encode(this);
	}
}