package io.vertx.webchat.websocket;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Session;
import io.vertx.webchat.comm.MessageHandlerImpl;

/**
 * This class handles the actual ServerWebSocket with a vertx-context.
 * It maps the sockets' frame-handler for messaging and information-exchange and
 * also the closing-handler, when a user disconnects.
 */
public class WebSocketManagerImpl implements WebSocketManager {
	private static final Logger log = LoggerFactory.getLogger(WebSocketManagerImpl.class);

	private final String sessionId;
	private final Session session;
	private Vertx vertx = null;
	private ServerWebSocket socket = null;
	private MessageHandlerImpl messageHandler = null;
	
	/**
	 * The frame-handler
	 * 
	 * @return
	 */
	private Handler<WebSocketFrame> getFrameHandler() {
		return handler -> {
			if (session.isDestroyed()) {
				log.error("session destroyed, rejecting socket");
				socket.reject();
				return;
			}

			System.out.println("got message from id: " + sessionId);
			messageHandler.broadcastMessage(sessionId, handler.textData());
		};
	}

	/**
	 * The closing handler
	 * 
	 * @return
	 */
	private Handler<Void> getCloseHandler() {
		return handler -> {
			log.debug("un-registering connection with id: " + sessionId);
		};
	}

	public WebSocketManagerImpl(Vertx vertx, ServerWebSocket ws, Session session) throws Exception {
		this.vertx = vertx;
		this.session = session;
		this.socket = ws;
		this.sessionId = ws.textHandlerID();

		this.messageHandler = new MessageHandlerImpl(vertx);
		initWebSocket();
	}

	private void initWebSocket() throws Exception {

		// TODO: correct exception type
		if (vertx == null || socket == null || session == null)
			throw new Exception("Missing or invalid arguments for WebSocketManager");

		final String sessionId = socket.textHandlerID();

		// User user = new User("user", "email");
		messageHandler.addActiveUser(session.getPrincipal(), sessionId);

		// TODO: Broadcast Message with new registered Id + online status
		System.out.println("registering new connection with id: " + sessionId);

		socket.closeHandler(getCloseHandler());

		socket.frameHandler(getFrameHandler());
	}
}
