package io.vertx.webchat.util;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Session;
import io.vertx.webchat.mapper.ContactMapper;
import io.vertx.webchat.util.WebSocketMessage.WebSocketMessageType;

import java.util.HashMap;

/**
 * This class handles the actual ServerWebSocket with a vertx-context.
 * It maps the sockets' frame-handler for messaging and information-exchange and
 * also the closing-handler, when a user disconnects.
 */
public class WebSocketManager {
	private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);

	private final String sessionId;
	private final Session session;
	private Vertx vertx = null;
	private ServerWebSocket socket = null;
	
	private static HashMap<String, JsonObject> userMap = new HashMap<String, JsonObject>();

	/**
	 * The frame-handler
	 * @return
	 */
	private Handler<WebSocketFrame> getFrameHandler() {
		return frame -> {
			if (session.isDestroyed()) {
				log.error("session destroyed, rejecting socket");
				socket.reject();
				return;
			}

			System.out.println("got message from id: " + sessionId);

		};
	}

	/**
	 * The closing handler for the websocket.
	 * This handler removes the current user from the list of online users
	 * @return
	 */
	private Handler<Void> getCloseHandler() {
		return handler -> {
			log.debug("un-registering connection with id: " + sessionId);
			
			userMap.remove(sessionId, session.getPrincipal());
		};
	}

	public WebSocketManager(Vertx vertx, ServerWebSocket ws, Session session) throws Exception {
		// TODO: correct exception type
		if (vertx == null || session == null || ws == null) {
			if(ws != null)
			ws.reject();
			
			throw new Exception("Missing or invalid arguments for WebSocketManager");
		}
		
		this.vertx = vertx;
		this.session = session;
		this.socket = ws;
		this.sessionId = ws.textHandlerID();
	
		// User user = new User("user", "email");
		userMap.put(sessionId, session.getPrincipal());

		socket.closeHandler(getCloseHandler());
		socket.frameHandler(getFrameHandler());
		
		JsonObject currentUser = session.getPrincipal();
		socket.writeFrame(new WebSocketMessage(currentUser, WebSocketMessageType.GetUserData,false));
		socket.writeFrame(new WebSocketMessage(ContactMapper.getContacts(currentUser.getInteger("uid")), WebSocketMessageType.GetContactList, false));

//		socket.writeFrame(WebSocketFrame.textFrame(new WebSocketMessage(currentUser, WebSocketMessageType.GetUserData,false).toString(), true));
//		Buffer data = Buffer.buffer().appendString(currentUser.encode());
//		socket.writeMessage(data);
//		EventBus bus = this.vertx.eventBus();
		
//		bus.publish(this.sessionId, new WebSocketMessage(currentUser, WebSocketMessageType.GetUserData,false).toString());
//		bus.publish(this.sessionId, new WebSocketMessage(ContactMapper.getContacts(currentUser.getInteger("uid")), WebSocketMessageType.GetContactList, false).toString());
		// TODO: Broadcast Message with new registered Id + online status
		log.debug("registering new connection with id: " + sessionId);
	}
}