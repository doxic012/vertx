package io.vertx.webchat.comm;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.webchat.models.User;

import java.util.HashMap;

public final class MessageHandler {
	private static String WEBCHAT_USERS_ONLINE = "webchat.users.online";

	private static HashMap<User, String> userMap = new HashMap<User, String>();
	/**
	 * Get a Map consisting of N-entries (<String, String>) with UserID,
	 * textHandlerID for the eventBus
	 * 
	 * @param vertx
	 * @return
	 */
	public static HashMap<User, String> getActiveUsers(Vertx vertx) {
//		SharedData data = vertx.sharedData();
		return userMap;//data.<String, String>getLocalMap(WEBCHAT_USERS_ONLINE);
	}

	public static void addActiveUser(Vertx vertx, User user, String textHandlerID) {
		HashMap<User, String> users = getActiveUsers(vertx);

		System.out.println("adding user " + textHandlerID);
		users.put(user, textHandlerID);
		System.out.println(users.size());
	}

	public static void broadcastMessage(Vertx vertx, String message) {
		HashMap<User, String> users = getActiveUsers(vertx);
		EventBus bus = vertx.eventBus();

		for (User key : users.keySet()) {
			System.out.println("Sending message to User " + key + " with handlerID: " + users.get(key));
			bus.send(users.get(key), message);
		}
	}

}
