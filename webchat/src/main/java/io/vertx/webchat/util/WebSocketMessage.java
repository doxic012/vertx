package io.vertx.webchat.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.FrameType;
import io.vertx.core.http.impl.ws.WebSocketFrameInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class WebSocketMessage implements WebSocketFrameInternal, ReferenceCounted {

	public enum WebSocketMessageType {
		GetUserData, SendMessageToUser, MessageRetrieved, GetMessageHistory, GetContactList, AddContact, RemoveContact, NotifyContact, UserOnlineStatus
	}

	private WebSocketMessageType messageType;

	private String messageData;

	private boolean isReply;

	private ByteBuf binaryData;

	public WebSocketMessage(String message, WebSocketMessageType messageType, boolean isReply) {
		this.messageType = messageType;
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

	public JsonObject toJson() {
		JsonObject message = new JsonObject().put("messageEvent", messageType).put("messageData", messageData).put("isReply", isReply);

		return message;
	}

	public void setMessageType(WebSocketMessageType messageEvent) {
		this.messageType = messageEvent;
	}

	public void setIsReply(boolean isReply) {
		this.isReply = isReply;
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
		return getBinaryData().toString(CharsetUtil.UTF_8);
	}

	@Override
	public Buffer binaryData() {
		return Buffer.buffer(getBinaryData());
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public ByteBuf getBinaryData() {
		return binaryData;
	}

	@Override
	// not allowed since we parse the object itself to json
	public void setBinaryData(ByteBuf binaryData) {
	}

	@Override
	public void setTextData(String messageData) {
		if (this.binaryData != null) {
			this.binaryData.release();
		}
		this.messageData = messageData;
		this.binaryData = Unpooled.copiedBuffer(this.toString(), CharsetUtil.UTF_8);
	}

	@Override
	public FrameType type() {
		return FrameType.TEXT;
	}

	@Override
	public int refCnt() {
		return binaryData.refCnt();
	}

	@Override
	public ReferenceCounted retain() {
		return binaryData.retain();
	}

	@Override
	public ReferenceCounted retain(int increment) {
		return binaryData.retain(increment);
	}

	@Override
	public boolean release() {
		return binaryData.release();
	}

	@Override
	public boolean release(int decrement) {
		return binaryData.release(decrement);
	}
}
