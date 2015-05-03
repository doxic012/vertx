package io.vertx.webchat.websocket;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

public interface WebSocketManager {

	/**
	 * Create a webSocket manager
	 *
	 * @param vertx the Vert.x instance
	 * @param ws The ServerWebSocket-instance
	 * @return the server websocket manager
	 */
	static WebSocketManager create(Vertx vertx, ServerWebSocket ws) throws Exception {
		return new WebSocketManagerImpl(vertx, ws);
	}
}
