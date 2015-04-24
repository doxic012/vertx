package io.vertx.webchat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Router;
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
import io.vertx.webchat.comm.MessageHandler;
import io.vertx.webchat.entity.User;

public class WebChatServer extends AbstractVerticle {
	private static String PATHS_CHAT = "/chat/*";

	@Override
	public void start() {
		System.out.println("started chat server");

		Router router = Router.router(vertx);
		
		LocalSessionStore sessions = LocalSessionStore.create(vertx);
		
		// We need cookies, sessions and request bodies
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(sessions));
		router.route().handler(BodyHandler.create()); // for request body

		// static resources for chat
		// router.route(WEBROOT_CHAT).handler(handler -> {
		// System.out.println("chat route");
		// handler.response().putHeader("location", "/chat/chatIndex.html");
		// });

		// Simple auth service which uses a properties file for user/role info
		AuthProvider authProvider = ShiroAuthProvider.create(vertx, ShiroAuthRealmType.PROPERTIES, new JsonObject());

		// Any requests to URI starting '/chat/' require login
		router.route(PATHS_CHAT).handler(RedirectAuthHandler.create(authProvider, "/login.html"));

		// Handles the actual login
		router.route("/login").handler(FormLoginHandler.create(authProvider));

		// Implement logout and redirect back to the index page
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
//		router.route().handler(handle -> {
//			System.out.println("is logged in:" + handle.session().isLoggedIn());
//			handle.next();
//		});
		router.route(PATHS_CHAT).handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("chat"));
		router.route().handler(StaticHandler.create());

		// create http-server on port 8080
		HttpServerOptions serverOptions = new HttpServerOptions().setMaxWebsocketFrameSize(100000);
		HttpServer server = vertx.createHttpServer(serverOptions).requestHandler(router::accept).websocketHandler(socket -> {
			
			if (!socket.path().matches(PATHS_CHAT)) {
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
					// socket.writeFrame(handler);

				} catch (Exception ex) {
					socket.reject();
				}

			});
		}).listen(8080);
	}
}
