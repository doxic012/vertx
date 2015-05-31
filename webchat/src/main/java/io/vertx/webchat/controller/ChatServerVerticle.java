package io.vertx.webchat.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.apex.handler.CookieHandler;
import io.vertx.ext.apex.handler.RedirectAuthHandler;
import io.vertx.ext.apex.handler.SessionHandler;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuthProvider;
import io.vertx.webchat.util.WebSocketManager;
import io.vertx.webchat.util.WebSocketMessage.WebSocketMessageType;
import io.vertx.webchat.util.auth.FormLoginRememberHandler;
import io.vertx.webchat.util.auth.FormRegistrationHandler;
import io.vertx.webchat.util.auth.HashInfo;
import io.vertx.webchat.util.auth.realm.ChatAuthRealm;

import java.time.LocalDate;

import org.apache.shiro.crypto.hash.Sha256Hash;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

		EventBus eb = vertx.eventBus();
		eb.consumer("chat.message.toServer", message -> {
			System.out.println("got message: " + message.body());
			String timestamp = LocalDate.now().toString();
			eb.publish("chat.message.toClient", timestamp + ": " + message.body());
		});

		// Handle WebSocket-requests to /chat using a WebSocket-Verticle for each connection
		router.route("/chat").handler(context -> {
			HttpServerRequest request = context.request();
			Session session = context.session();

			// login required to establish websocket connection
			if (session.isLoggedIn()) {
				ServerWebSocket socket = request.upgrade();

				try {
					WebSocketManager manager = new WebSocketManager(socket, session);

					manager.setMessageEvent(WebSocketMessageType.SendMessage, message -> {
						JsonObject data = new JsonObject(message.getMessageData().toString());
						System.out.println("send message event. data: " + data.encodePrettily());
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				context.fail(403);
			}
		});

		// Map all requests to /chat/* to a redirect-handler that sends the user
		// to the loginpage
		// Using the custom chat authentication realm with hibernate
		AuthProvider authProvider = ShiroAuthProvider.create(vertx, new ChatAuthRealm(hashInfo));
		router.route("/chat/*").handler(RedirectAuthHandler.create(authProvider, "/"));

		// Handles the registration
		router.route("/register").handler(FormRegistrationHandler.create(hashInfo, authProvider));

		// Handle login by html-form and pass principle-data to the session after a successful login
		router.route("/login").handler(FormLoginRememberHandler.create(authProvider));

		// Handle logout
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

		// HttpServerOptions serverOptions = new HttpServerOptions().setMaxWebsocketFrameSize(100000);
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		
//		TestObject msg = new TestObject(1, 2);
////		String encode = "{\"testobject\":{\"a\":\"test\",\"b\":\"data\"}}";
//String encode = Json.encode(msg);
//		System.out.println(encode);
//		msg = Json.decodeValue(encode, TestObject.class);
	}
	
//	@JsonIgnoreProperties(ignoreUnknown=true)
//	class TestObject {
//		@JsonProperty(value = "a")
//		public int a;
//		@JsonProperty(value = "b")
//		public int b;
//
//		@JsonCreator
//		public TestObject(@JsonProperty(value="a") int a, @JsonProperty(value="b") int b) {
//			this.a = a;
//			this.b = b;
//		}
//
//	}
}
