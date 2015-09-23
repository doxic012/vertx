package io.vertx.webchat.util;

import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class WebSocketMessage {

	public enum MessageType {
		USER_STATUS, USER_DATA, USER_LIST,
		MESSAGE_SEND, MESSAGE_READ, MESSAGE_HISTORY,
		CONTACT_LIST, CONTACT_ADD, CONTACT_REMOVE, CONTACT_NOTIFIED,
	}

	private MessageType messageType;

	private Object messageData; // Inhalt

	private JsonObject origin = new JsonObject().put("name", "server"); // wer verschickt? server/client

	private JsonObject target;

	private Timestamp timestamp;

	private boolean reply; // Ist die Nachricht eine Rückmeldung auf eine andere Nachricht

	/**
	 * Json.decodeValue braucht einen leeren Constructor, um die WebSocketMessage vom Client auf den Server ordentlich
	 * zu deserialisieren (verwendet setter)
	 * 
	 * @JsonIgnore und @JsonCreator (jackson annotation) wird verwendet, um Json.decodeValue mitzuteilen, welcher
	 *             Constructor verwendet wird zum dekodieren
	 */
	@JsonCreator
	public WebSocketMessage() {

	}

	@JsonIgnore
	public WebSocketMessage(MessageType messageType, Object messageData, JsonObject target) {
		this.setMessageType(messageType);
		this.setMessageData(messageData);
		this.setTarget(target);
		this.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
	}

	@JsonIgnore
	public WebSocketMessage(MessageType messageType, Object messageData, JsonObject origin, JsonObject target) {
		this.setMessageType(messageType);
		this.setMessageData(messageData);
		this.setOrigin(origin);
		this.setTarget(target);
		this.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
	}

	@JsonIgnore
	public WebSocketMessage(MessageType messageType, Object messageData) {
		this.setMessageType(messageType);
		this.setMessageData(messageData);
		this.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
		this.setReply(reply);
	}

	/**
	 * Baut einen Frame, der per Websocket verschickt werden kann
	 * 
	 * @return
	 */
	public WebSocketFrame toFrame() {
		return WebSocketFrame.textFrame(this.toString(), true);
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public WebSocketMessage setMessageType(MessageType messageType) {
		this.messageType = messageType;
		return this;
	}

	public Object getMessageData() {
		return messageData;
	}

	public WebSocketMessage setMessageData(Object messageData) {
		this.messageData = messageData;
		return this;
	}

	public JsonObject getOrigin() {
		return origin;
	}

	public WebSocketMessage setOrigin(JsonObject origin) {
		this.origin = origin;
		return this;
	}

	public JsonObject getTarget() {
		return target;
	}

	public WebSocketMessage setTarget(JsonObject target) {
		this.target = target;
		return this;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public WebSocketMessage setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public boolean getReply() {
		return reply;
	}

	public WebSocketMessage setReply(boolean isReply) {
		this.reply = isReply;
		return this;
	}

	@Override
	public String toString() {
		return Json.encode(this);
	}
}