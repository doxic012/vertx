package io.vertx.webchat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.apex.handler.CookieHandler;
import io.vertx.ext.apex.handler.FormLoginHandler;
import io.vertx.ext.apex.handler.RedirectAuthHandler;
import io.vertx.ext.apex.handler.SessionHandler;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.webchat.server.websocket.WebSocketConfigurator;

public class ChatServer extends AbstractVerticle {

	@Override
	public void start() {
		System.out.println("started chat server");
		// create http-server on port 8080
		Router router = Router.router(vertx);

		LocalSessionStore sessionStore = LocalSessionStore.create(vertx);

		// We need cookies, sessions and request bodies
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(sessionStore));
		router.route().handler(BodyHandler.create()); // for request body
		
		// Route for websocket upgrade-request with authentication handling
		router.route("/chat").handler(context -> {

			HttpServerRequest request = context.request();
			Session session = context.session();
			System.out.println("user logged in: " + session.isLoggedIn());

			if (session.isLoggedIn()) {
				ServerWebSocket socket = request.upgrade();

				try {
					WebSocketConfigurator.configureWebSocket(vertx, socket);
				} catch (Exception ex) {
					ex.printStackTrace();
					
					if(socket != null)
					socket.reject();
					return;
				}
			}

			context.response().setStatusCode(403).end();
		});
		
		// Any requests to URI starting '/chat' require login
		// Simple auth service which uses a properties file for user/role info
		AuthProvider authProvider = ShiroAuthProvider.create(vertx, ShiroAuthRealmType.PROPERTIES, new JsonObject());
		router.route("/chat/*").handler(RedirectAuthHandler.create(authProvider, "/login.html"));

		
		// Handles the actual login and logout
		router.route("/login").handler(FormLoginHandler.create(authProvider));
		router.route("/logout").handler(context -> {
			System.out.println("logging out");
			context.session().logout();
			context.response().putHeader("location", "/").setStatusCode(302).end();
		});

		// failure handler
		router.get().failureHandler(failureHandler -> {
			int code = failureHandler.statusCode();

			HttpServerResponse response = failureHandler.response();
			response.setStatusCode(code).end("Failed to process: Status Code: " + code);
		});

		// static resources (css, js, ...)
		// StaticHandlers ALWAYS represent the final invocation!
		router.route("/chat/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("chat"));
		router.route().handler(StaticHandler.create());

		HttpServerOptions serverOptions = new HttpServerOptions().setMaxWebsocketFrameSize(100000);
		HttpServer server = vertx.createHttpServer(serverOptions).requestHandler(handler -> {
			System.out.println("request: " + handler.absoluteURI());

			router.accept(handler);
		})
		.listen(8080);
	}
}
