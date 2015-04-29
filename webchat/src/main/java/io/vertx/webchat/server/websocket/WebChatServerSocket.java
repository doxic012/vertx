package io.vertx.webchat.server.websocket;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.webchat.comm.MessageHandler;
import io.vertx.webchat.entity.User;

public class WebChatServerSocket implements Handler<ServerWebSocket> {

	private Vertx vertx = null;
	private String webSocketPath = "/chat/*";
	private LocalSessionStore sessionStore = null;

	public WebChatServerSocket(Vertx vertx, String webSocketPath, LocalSessionStore sessionStore) {
		this.vertx = vertx;
		this.webSocketPath = webSocketPath;
		this.sessionStore = sessionStore;
	}

	@Override
	public void handle(ServerWebSocket socket) {
		final String sessionId = socket.textHandlerID();

		if (!socket.path().matches(webSocketPath)) {
			socket.reject();
			return;
		}
		User user = new User("user", "email");
		MessageHandler.addActiveUser(vertx, user, sessionId);
		System.out.println("registering new connection with id: " + sessionId);

		socket.closeHandler(handler -> {
			System.out.println("un-registering connection with id: " + sessionId);
		});

		socket.frameHandler(handler -> {
			try {
				System.out.println("Framehandler for id: " + sessionId);
				MessageHandler.broadcastMessage(vertx, handler.textData());
				// socket.writeFrame(handler);

			} catch (Exception ex) {
				socket.reject();
			}
		});

		// if (sessionStore == null)
		// socket.reject();
		//
		// sessionStore.get(sessionId, sessionHandler -> {
		// try {
		// if(sessionHandler.failed())
		// socket.reject();
		//
		// System.out.println("sessionstore get");
		// Session session = sessionHandler.result();
		//
		// System.out.println("session is null: "+(session == null));
		//
		// if(session != null)
		// System.out.println("user logged in: "+session.isLoggedIn());
		//
		// if (session == null || !session.isLoggedIn())
		// socket.reject();
		// } catch (Exception ex) {
		// System.out.println("Exception, rejecting Socket");
		// ex.printStackTrace();
		// socket.reject();
		// }
		//
		// });

	}

}
