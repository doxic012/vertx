package io.vertx.webchat.server.websocket;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.webchat.comm.MessageHandler;
import io.vertx.webchat.entity.User;

public final class WebSocketConfigurator {

	public static void configureWebSocket(Vertx vertx, ServerWebSocket ws)
			throws Exception {
		
		if (vertx == null || ws == null)
			throw new Exception("Missing or invalid arguments for ChatWebSocketManager");

		final String sessionId = ws.textHandlerID();
		User user = new User("user", "email");
		MessageHandler.addActiveUser(vertx, user, sessionId);

		System.out.println("registering new connection with id: " + sessionId);

		ws.closeHandler(handler -> {
			System.out.println("un-registering connection with id: " + sessionId);
		});

		ws.frameHandler(handler -> {
			System.out.println("Framehandler for id: " + sessionId);
			MessageHandler.broadcastMessage(vertx, handler.textData());
		});
	}
}
