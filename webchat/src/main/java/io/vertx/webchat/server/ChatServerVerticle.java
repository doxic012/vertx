package io.vertx.webchat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.apex.handler.CookieHandler;
import io.vertx.ext.apex.handler.RedirectAuthHandler;
import io.vertx.ext.apex.handler.SessionHandler;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuthProvider;
import io.vertx.webchat.auth.handler.FormLoginRememberHandler;
import io.vertx.webchat.auth.handler.FormRegistrationHandler;
import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.auth.realm.ChatAuthRealm;
import io.vertx.webchat.hibernate.HibernateUtil;
import io.vertx.webchat.models.User;
import io.vertx.webchat.websocket.WebSocketVerticle;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class ChatServerVerticle extends AbstractVerticle {

	private HashInfo hashInfo = new HashInfo(Sha256Hash.ALGORITHM_NAME, 1024, false);

	@Override
	public void start() {
		// create http-server on port 8080
		Router router = Router.router(vertx);

		// Handlers for cookies, sessions and request bodies
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		router.route().handler(BodyHandler.create());

		// Routing the upgrade-request for the websocket and initiating it with predefined frame- and closing-handlers.
		// This route is necessary to check the current login status of each user and prevents websocket-connections
		// from outside
		router.route("/chat").handler(context -> {
			HttpServerRequest request = context.request();
			Session session = context.session();

			// login required to establish websocket connection
			if (session.isLoggedIn()) {
				ServerWebSocket socket = request.upgrade();
				vertx.deployVerticle(new WebSocketVerticle(session, socket), res -> {
					System.out.println("verticle deployed, result: " + res.succeeded() + "; id: " + res.result());
				});

				// try {
				// WebSocketManager socketManager = WebSocketManager.create(vertx, socket, session);
				// } catch (Exception ex) {
				// ex.printStackTrace();
				//
				// if (socket != null)
				// socket.reject();
				// return;
				// }
			} else {
				context.fail(403);
			}
		});

		// Map all requests to /chat/* to a redirect-handler that sends the user
		// to the loginpage
		// Using the custom chat authentication realm with hibernate
		AuthProvider authProvider = ShiroAuthProvider.create(vertx, new ChatAuthRealm(HibernateUtil.getSessionFactory(), hashInfo));
		router.route("/chat/*").handler(RedirectAuthHandler.create(authProvider, "/"));
		
		// Handles the registration
		router.route("/register").handler(FormRegistrationHandler.create(hashInfo, authProvider));

		
		router.route("/register").handler(new Handler<RoutingContext>() {
			
			@Override
			public void handle(RoutingContext context) {
				
			}
		});
		// Handle login by html-form and pass principle-data to the session after a successful login
		router.route("/login").handler(FormLoginRememberHandler.create(authProvider, credentials -> {
			return User.getUser(credentials).toJson();
		}));
		
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
		// StaticHandlers always need to be the final routings!
		// router.route("/chat/*").handler(StaticHandler.create().setCachingEnabled(false));//.setWebRoot("chat"));
		router.route().handler(StaticHandler.create());

		HttpServerOptions serverOptions = new HttpServerOptions().setMaxWebsocketFrameSize(100000);
		vertx.createHttpServer(serverOptions).requestHandler(router::accept).listen(8080);
	}
}
