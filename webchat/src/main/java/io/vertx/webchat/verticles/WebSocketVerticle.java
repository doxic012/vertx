package io.vertx.webchat.verticles;

import io.vertx.core.AbstractVerticle;
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
public class WebSocketVerticle extends AbstractVerticle {
	private static final Logger log = LoggerFactory.getLogger(WebSocketVerticle.class);

	private final String sessionId;
	private final Session session;
	private final ServerWebSocket socket;
	private MessageHandlerImpl messageHandler = null;

	public WebSocketVerticle(Session session, ServerWebSocket socket) {
		this.session = session;
		this.socket = socket;
		this.sessionId = socket.textHandlerID();
	}
	
	@Override
	public void start() {
		System.out.println("WebSocket Verticle started");
		System.out.println("session principle: " + session.getPrincipal().getString("username"));
		System.out.println("socketId: " + sessionId);

		// TODO: correct exception type
		if (vertx == null || socket == null || session == null) {
			System.out.println("Missing or invalid arguments for WebSocketManager");
			socket.reject();
			return;
		}

		this.messageHandler = new MessageHandlerImpl(vertx);
		
		// User user = new User("user", "email");
		messageHandler.addActiveUser(session.getPrincipal(), sessionId);

		// TODO: Broadcast Message with new registered Id + online status
		System.out.println("registering new connection with id: " + sessionId);

		socket.closeHandler(getCloseHandler());
		socket.frameHandler(getFrameHandler());
	}

	/**
	 * The frame-handler
	 * 
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
//			messageHandler.broadcastMessage(sessionId, frame.textData());
		};
	}

	/**
	 * The closing handler
	 * 
	 * @return
	 */
	private Handler<Void> getCloseHandler() {
		return frame -> {
			log.debug("un-registering connection with id: " + sessionId);
		};
	}
}
