package io.vertx.webchat.util;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Session;
import io.vertx.webchat.mapper.ContactMapper;
import io.vertx.webchat.util.WebSocketMessage.WebSocketMessageType;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class handles the actual ServerWebSocket with a vertx-context.
 * It maps the sockets' frame-handler for messaging and information-exchange and
 * also the closing-handler, when a user disconnects.
 */
public class WebSocketManager {
	private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);

	private final String sessionId;
	private final Session session;
	private ServerWebSocket socket = null;

	private static HashMap<ServerWebSocket, String> userMap = new HashMap<ServerWebSocket, String>();

	private static HashMap<String, Handler<WebSocketMessage>> socketEvents = new HashMap<String, Handler<WebSocketMessage>>();

	/**
	 * The frame-handler
	 * @return
	 */
	private Handler<WebSocketFrame> getFrameHandler() {
		return frame -> {
			if (session.isDestroyed()) {
				log.error("session destroyed, rejecting socket");

				userMap.remove(socket, session.getPrincipal());
				socket.reject();
				return;
			}

			try {
				System.out.println("got message from id: " + sessionId + ", message:" + frame.textData());
				WebSocketMessage message = Json.decodeValue(frame.textData(), WebSocketMessage.class);

				System.out.println("type: " + message.getMessageType().toString());
				
				// handle the frame
				if(socketEvents.containsKey(message.getMessageType().toString()))
					socketEvents.get(message.getMessageType().toString()).handle(message);

			} catch (Exception ex) {
				ex.printStackTrace();
				log.debug("Exception caught in websocket message - no applicable message type");
			}
		};
	}

	/**
	 * The closing handler for the websocket. 	
	 * This handler removes the current user from the list of online users
	 * @return
	 */
	private Handler<Void> getCloseHandler() {
		return handler -> {
			userMap.remove(socket, session.getPrincipal());
			broadcastMessage(new WebSocketMessage(WebSocketMessageType.UserOffline, session.getPrincipal()));

			log.debug("un-registering new connection with id: " + sessionId + ", users online: " + userMap.size());
		};
	}

	public void setMessageEvent(WebSocketMessageType type, Handler<WebSocketMessage> handler) {
		setMessageEvent(type.toString(), handler);
	}

	public void setMessageEvent(String type, Handler<WebSocketMessage> handler) {
		if (!socketEvents.containsKey(type))
			socketEvents.put(type, handler);
		else
			socketEvents.replace(type, handler);
	}
	
	public void broadcastMessage(WebSocketMessage message) {
		JsonObject currentUser = session.getPrincipal();

		userMap.forEach((socket, email) -> {
			if (!email.equals(currentUser.getString("email"))) {
				writeMessage(socket, message);
			}
		});
	}

	public void writeMessage(WebSocketMessage msg) {
		this.socket.writeFrame(msg.toFrame());
	}
	
	public void writeMessage(ServerWebSocket socket, WebSocketMessage msg) {
		socket.writeFrame(msg.toFrame());
	}
	public WebSocketManager(ServerWebSocket ws, Session session) throws Exception {
		
		// TODO: correct exception type
		if (session == null || ws == null) {
			if (ws != null)
				ws.reject();

			throw new Exception("Missing or invalid arguments for WebSocketManager");
		}

		this.session = session;
		this.socket = ws;
		this.sessionId = ws.textHandlerID();
		JsonObject currentUser = session.getPrincipal();

		// User user = new User("user", "email");
		userMap.put(socket, currentUser.getString("email"));

		// TODO: Broadcast Message with new registered Id + online status
		log.debug("registering new connection with id: " + sessionId + ", users online: " + userMap.size());

		socket.closeHandler(getCloseHandler());
		socket.frameHandler(getFrameHandler());

		writeMessage(new WebSocketMessage(WebSocketMessageType.GetUserData, currentUser));
		writeMessage(new WebSocketMessage(WebSocketMessageType.GetContactList, ContactMapper.getContacts(currentUser.getInteger("uid"))));

		// broadcast online status to everyone except us
		broadcastMessage(new WebSocketMessage(WebSocketMessageType.UserOnline, currentUser));
	}

}