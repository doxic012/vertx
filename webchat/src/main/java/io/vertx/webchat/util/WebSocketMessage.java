package io.vertx.webchat.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.FrameType;
import io.vertx.core.http.impl.ws.WebSocketFrameInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;

public class WebSocketMessage {

	public enum WebSocketMessageType {
		GetUserData, SendMessage, MessageRetrieved, GetMessageHistory, GetContactList, AddContact, RemoveContact, NotifyContact, UserOnline, UserOffline
	}

	private String messageType;

	private Object messageData;

	private boolean isReply;

	public WebSocketMessage(String messageType, Object messageData, boolean isReply) {
		this.setMessageType(messageType);
		this.setMessageData(messageData);
		this.setReply(isReply);
		
		System.out.println(this.toString());
	}
	
	public WebSocketMessage(WebSocketMessageType messageType, Object messageData, boolean isReply) {
		this(messageType.toString(), messageData, isReply);
	}
	
	public WebSocketMessage(WebSocketMessageType messageType, Object messageData) {
		this(messageType.toString(), messageData, false);
	}
	
	public WebSocketMessage(String messageType, Object messageData) {
		this(messageType, messageData, false);
	}
	



	public WebSocketMessageFrame toFrame() {
		return new WebSocketMessageFrame(this);
	}
	
	public String getMessageType() {
		return messageType;
	}

	private void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	public boolean isReply() {
		return isReply;
	}

	private void setReply(boolean isReply) {
		this.isReply = isReply;
	}

	public Object getMessageData() {
		return messageData;
	}

	private void setMessageData(Object messageData) {
		this.messageData = messageData;
	}

	@Override
	public String toString() {
		return Json.encode(this);
	}
	
	private class WebSocketMessageFrame implements WebSocketFrameInternal, ReferenceCounted {
		
		private ByteBuf binaryData;
		
		private WebSocketMessage message;
		
		private WebSocketMessageFrame(WebSocketMessage message) {
			this.message = message;
			this.binaryData = Unpooled.copiedBuffer(message.toString(), CharsetUtil.UTF_8);
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
			
			message.setMessageData(messageData);
			this.binaryData = Unpooled.copiedBuffer(message.toString(), CharsetUtil.UTF_8);
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
}