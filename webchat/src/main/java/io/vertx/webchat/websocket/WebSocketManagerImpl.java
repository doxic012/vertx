package io.vertx.webchat.websocket;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.webchat.comm.MessageHandler;

/**
 * This class handles the actual ServerWebSocket with a vertx-context.
 * It maps the sockets' frame-handler for messaging and information-exchange and
 * also the closing-handler, when a user disconnects.
 */
public class WebSocketManagerImpl implements WebSocketManager {

	private String sessionId = null;
//	private final LocalMap<String, User>
	private Vertx vertx = null;
	private ServerWebSocket socket = null;

	/**
	 * The frame-handler
	 * @return
	 */
	private Handler<WebSocketFrame> getFrameHandler() {
		return handler -> {
			System.out.println("Framehandler for id: " + sessionId);
			MessageHandler.broadcastMessage(vertx, handler.textData());
		};
	}

	/**
	 * The closing handler
	 * @return
	 */
	private Handler<Void> getCloseHandler() {
		return handler -> {
			System.out.println("un-registering connection with id: " + sessionId);
		};
	}

	public WebSocketManagerImpl(Vertx vertx, ServerWebSocket ws) throws Exception {
		if (vertx == null || ws == null)
			throw new Exception("Missing or invalid arguments for WebSocketManager");

		this.vertx = vertx;
//		this.sessionStore = sessions;
		this.socket = ws;
		this.sessionId = ws.textHandlerID();
		
		initWebSocket();
	}

	private void initWebSocket() {


		final String sessionId = socket.textHandlerID();
//		User user = new User("user", "email");
//		MessageHandler.addActiveUser(vertx, user, sessionId);

		// TODO: Broadcast Message with new registered Id + online status
		System.out.println("registering new connection with id: " + sessionId);

		socket.closeHandler(getCloseHandler());

		socket.frameHandler(getFrameHandler());
	}
}
