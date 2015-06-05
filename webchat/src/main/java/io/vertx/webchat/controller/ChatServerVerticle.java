package io.vertx.webchat.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
import io.vertx.webchat.mapper.ContactMapper;
import io.vertx.webchat.mapper.MessageMapper;
import io.vertx.webchat.mapper.UserMapper;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.WebSocketManager;
import io.vertx.webchat.util.WebSocketMessage;
import io.vertx.webchat.util.WebSocketMessage.MessageType;
import io.vertx.webchat.util.auth.FormLoginRememberHandler;
import io.vertx.webchat.util.auth.FormRegistrationHandler;
import io.vertx.webchat.util.auth.HashInfo;
import io.vertx.webchat.util.auth.realm.ChatAuthRealm;

import java.io.IOException;
import java.time.LocalDate;

import org.apache.shiro.crypto.hash.Sha256Hash;

public class ChatServerVerticle extends AbstractVerticle {

	private HashInfo hashInfo = new HashInfo(Sha256Hash.ALGORITHM_NAME, 1024, false);

	@Override
	public void start() throws IOException {

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

					manager.addEvent(MessageType.MESSAGE_SEND, message -> {
						JsonObject origin = message.getOrigin();
						JsonObject target = message.getTarget();

						System.out.println("message send event. data: " + message.getMessageData() + ", target: " + target);

						boolean targetOnline = manager.getUserConnections().containsPrincipal(target);
						boolean status = MessageMapper.addMessage(origin.getInteger("uid"), target.getInteger("uid"), (String) message.getMessageData());

						// reply to the user with a status message of the storages process
						manager.writeMessage(message.setMessageData(status).setReply(true));

						// send message to target only if the storage process was successful
						if (status) {
							if (targetOnline)
								manager.writeMessageToPrincipal(target, message);
							else
								ContactMapper.setNotification(origin.getInteger("uid"), target.getInteger("uid"));
						}
					});
					manager.addEvent(MessageType.MESSAGE_READ, message -> {
						JsonObject origin = message.getOrigin();
						JsonObject target = message.getTarget();

						System.out.println("message read event. data: " + message.getMessageData() + ", target: " + target);

						// Notify the original sender of a message that the target has read it
						manager.writeMessageToPrincipal(target, message);
					});
					manager.addEvent(MessageType.MESSAGE_HISTORY, message -> {
						JsonObject origin = message.getOrigin();
						JsonObject target = message.getTarget();
						int count = (int) message.getMessageData();

						System.out.println("get message history event. data: " + message.getMessageData() + ", target: " + target);

						JsonArray history = MessageMapper.getMessages(origin.getInteger("uid"), target.getInteger("uid"), count);
						manager.writeMessageToPrincipal(origin, message.setMessageData(history).setReply(true));
					});
					manager.addEvent(MessageType.CONTACT_ADD, message -> {
						JsonObject origin = message.getOrigin();
						JsonObject target = message.getTarget();

						// TODO: Send request to target?

						System.out.println("add contact event. data: " + message.getMessageData() + ", target: " + target);

						// Add target to the contact list and reply the modified contact list to the user
						boolean status = ContactMapper.addContact(origin.getInteger("uid"), target.getInteger("uid"));

						if (status) {
							JsonArray contactList = ContactMapper.getContacts(origin.getInteger("uid"));
							manager.writeMessageToPrincipal(origin, new WebSocketMessage(MessageType.CONTACT_LIST, contactList));
						}

					});
					manager.addEvent(MessageType.CONTACT_REMOVE, message -> {
						JsonObject origin = message.getOrigin();
						JsonObject target = message.getTarget();

						System.out.println("remove contact event. data: " + message.getMessageData() + ", target: " + target);

						// Remove target from the contact list and reply the modified contact list to the user
						boolean status = ContactMapper.removeContact(origin.getInteger("uid"), target.getInteger("uid"));

						if (status) {
							JsonArray contactList = ContactMapper.getContacts(origin.getInteger("uid"));
							manager.writeMessageToPrincipal(origin, new WebSocketMessage(MessageType.CONTACT_LIST, contactList));
						}
					});
					manager.addEvent(MessageType.CONTACT_LIST, message -> {
						JsonObject origin = message.getOrigin();

						System.out.println("get contact list event. data: " + message.getMessageData() + ", origin: " + origin);

						// Reply the contact list
						JsonArray contacts = ContactMapper.getContacts(origin.getInteger("uid"));
						manager.writeMessageToPrincipal(origin, message.setMessageData(contacts).setReply(true));
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

		
		JsonArray a=ContactMapper.getContacts(7);
		System.out.println(a);
		//		org.hibernate.Session connectSession = HibernateUtil.getSession();

//		try {
//			// contactList = (List<Contact>)
//			// connectSession.createQuery("FROM Contact WHERE uid=:uid").setParameter("uid", uid).list();
//			User user = (User) connectSession.createQuery("FROM User WHERE uid=:uid").setParameter("uid", 7).uniqueResult();
//			
//			System.out.println(user.getContacts().size());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}