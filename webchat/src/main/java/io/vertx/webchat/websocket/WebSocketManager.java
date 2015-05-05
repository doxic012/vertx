package io.vertx.webchat.websocket;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.apex.Session;

public interface WebSocketManager {

	/**
	 * Create a webSocket manager
	 *
	 * @param vertx the Vert.x instance
	 * @param ws The ServerWebSocket-instance
	 * @param session the current session for a user-object
	 * @return the server websocket manager
	 */
	static WebSocketManager create(Vertx vertx, ServerWebSocket ws, Session session) throws Exception {
		return new WebSocketManagerImpl(vertx, ws, session);
	}
}
