package io.vertx.webchat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.handler.sockjs.BridgeOptions;
import io.vertx.ext.apex.handler.sockjs.PermittedOptions;
import io.vertx.webchat.comm.MessageHandler;
import io.vertx.webchat.entity.User;

public class WebChatServer extends AbstractVerticle {
	private static String WEBSOCKET_CHAT_PATH = "/chat";

	public static String getWebSocketPath() {
		return WEBSOCKET_CHAT_PATH;
	}

	// options for bridging the sockJS-handler to the clientside-js
	private BridgeOptions getSockJSBridgeOptions() {
		BridgeOptions options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress("webchat.msg.client")).addInboundPermitted(new PermittedOptions().setAddress("webchat.msg.server"));

		// TODO: Authentication
		// .setRequiredRole
		// http://vert-x3.github.io/docs/vertx-apex/java/#_requiring_authorisation_for_messages
		return options;
	}

	@Override
	public void start() {
		System.out.println("started chat server");

		Router router = Router.router(vertx);

		// router.route().handler(CookieHandler.create());
		// router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		router.route().handler(StaticHandler.create());

		// sockjs handler
		// SockJSHandlerOptions options = new
		// SockJSHandlerOptions().setHeartbeatPeriod(2000);
		// SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
		// .bridge(getSockJSBridgeOptions());

		// router.route("/chat/*").handler(sockJSHandler);

		// failure handler
		router.get().failureHandler(failureHandler -> {
			int code = failureHandler.statusCode();

			HttpServerResponse response = failureHandler.response();
			response.setStatusCode(code).end("Failed to process: Status Code: " + code);
		});

		// create http-server on port 8080
		HttpServerOptions serverOptions = new HttpServerOptions().setMaxWebsocketFrameSize(100000);
		HttpServer server = vertx.createHttpServer(serverOptions).requestHandler(router::accept).websocketHandler(socket -> {

			if (!socket.path().equals(WEBSOCKET_CHAT_PATH)) {
				socket.reject();
				return;
			}

			final String id = socket.textHandlerID();
			User user = new User("user", "email");

			MessageHandler.addActiveUser(vertx, user, id);

			System.out.println("registering new connection with id: " + id);

			socket.closeHandler(handler -> {
				System.out.println("un-registering connection with id: " + id);
			});

			socket.frameHandler(handler -> {
				try {
					System.out.println("Framehandler for id: " + id);
					MessageHandler.broadcastMessage(vertx, handler.textData());
//					socket.writeFrame(handler);

				} catch (Exception ex) {
					socket.reject();
				}

			});
		}).listen(8080);

		// EventBus eb = vertx.eventBus();
		//
		// // Register to listen for messages coming IN to the server
		// eb.consumer("webchat.msg.server").handler(message -> {
		//
		// System.out.println("Got message at event bus: " + message);
		//
		// // Create a timestamp string
		// String timestamp = LocalDate.now().toString();
		//
		// // Send the message back out to all clients with the timestamp
		// // prepended.
		// eb.publish("webchat.msg.client", timestamp + ": " + message.body());
		// });

	}
}
