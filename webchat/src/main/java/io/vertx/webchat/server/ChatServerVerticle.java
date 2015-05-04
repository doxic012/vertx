package io.vertx.webchat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.apex.handler.CookieHandler;
import io.vertx.ext.apex.handler.RedirectAuthHandler;
import io.vertx.ext.apex.handler.SessionHandler;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuthProvider;
import io.vertx.webchat.auth.handler.FormLoginRememberHandler;
import io.vertx.webchat.auth.handler.FormRegistrationHandler;
import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.auth.realm.ChatAuthRealm;
import io.vertx.webchat.hibernate.HibernateUtil;
import io.vertx.webchat.models.User;
import io.vertx.webchat.sessions.ChatSessionStore;
import io.vertx.webchat.websocket.WebSocketManager;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class ChatServerVerticle extends AbstractVerticle {

	private HashInfo hashInfo = new HashInfo(Sha256Hash.ALGORITHM_NAME, 1024, false);

	/**
	 * Register a new user with a username, email an password.
	 * Optional: Admin role
	 *
	 * @param session
	 * @param username
	 * @param email
	 * @param plainTextPassword
	 * @param isAdmin
	 */
	private void registerUser(org.hibernate.Session session, HashInfo hashingInfo, String username, String email, String plainTextPassword) {

		ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
		SimpleHash hash = new SimpleHash(hashingInfo.getAlgorithmName(), plainTextPassword, salt, hashingInfo.getIterations());

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setRoleNames("user");

		// Generate hashing-function and encode password
		user.setPassword(hashingInfo.isHexEncoded() ? hash.toHex() : hash.toBase64());
		user.setSalt(salt.toString());

		System.out.println("User with email:" + user.getEmail() + " hashedPassword:" + user.getPassword() + " salt:" + user.getSalt());

		session.save(user);

	}

	@Override
	public void start() {

		// create http-server on port 8080
		Router router = Router.router(vertx);

		// The custom chat session-store
		ChatSessionStore sessionStore = ChatSessionStore.create(vertx);

		// Handlers for cookies, sessions and request bodies
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(sessionStore));
		router.route().handler(BodyHandler.create()); // for request body

		// Routing the upgrade-request for the websocket and initiating it with
		// predefined frame- and closing-handlers.
		// This route is necessary to check the current login status of each
		// user and prevents websocket-connections from outside
		router.route("/chat").handler(context -> {
			HttpServerRequest request = context.request();
			Session session = context.session();

			// login required to establish websocket connection
			if (session.isLoggedIn()) {
				ServerWebSocket socket = request.upgrade();

				try {
					WebSocketManager socketManager = WebSocketManager.create(vertx, socket);
				} catch (Exception ex) {
					ex.printStackTrace();

					if (socket != null)
						socket.reject();
					return;
				}
			} else {
				context.fail(403);
			}
		});

		// Map all requests to /chat/* to a redirect-handler that sends the user
		// to the loginpage
		// Using the custom chat authentication realm with hibernate
		AuthProvider authProvider = ShiroAuthProvider.create(vertx, new ChatAuthRealm(HibernateUtil.getSessionFactory(), hashInfo));
		router.route("/chat/*").handler(RedirectAuthHandler.create(authProvider, "/login.html"));

		// Handles the registration
		router.route("/register").handler(FormRegistrationHandler.create(handler -> {
			org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();

			String username = handler.getString(FormRegistrationHandler.DEFAULT_USERNAME_PARAM);
			String email = handler.getString(FormRegistrationHandler.DEFAULT_EMAIL_PARAM);
			String password = handler.getString(FormRegistrationHandler.DEFAULT_PASSWORD_PARAM);

			try {
				registerUser(session, hashInfo, username, email, password);
			} finally {
				session.getTransaction().commit();
				if (session.isOpen())
					session.close();
			}
		}));

		// Handles the actual login and logout
		router.route("/login").handler(FormLoginRememberHandler.create(authProvider));
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
		router.route("/chat/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("chat"));
		router.route().handler(StaticHandler.create());

		HttpServerOptions serverOptions = new HttpServerOptions().setMaxWebsocketFrameSize(100000);
		vertx.createHttpServer(serverOptions).requestHandler(router::accept).listen(8080);
	}
}
