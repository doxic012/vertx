package io.vertx.webchat.comm;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

public final class MessageHandler {
	private static String WEBCHAT_USERS_ONLINE = "webchat.users.online";

	private static HashMap<JsonObject, String> userMap = new HashMap<JsonObject, String>();

	/**
	 * Get a Map consisting of N-entries (<String, String>) with UserID,
	 * textHandlerID for the eventBus
	 * 
	 * @param vertx
	 * @return
	 */
	public static HashMap<JsonObject, String> getActiveUsers(Vertx vertx) {
		// SharedData data = vertx.sharedData();
		return userMap;// data.<String, String>getLocalMap(WEBCHAT_USERS_ONLINE);
	}

	public static void addActiveUser(Vertx vertx, JsonObject principal, String textHandlerID) {
		HashMap<JsonObject, String> users = getActiveUsers(vertx);

		System.out.println("adding user " + textHandlerID);
		users.put(principal, textHandlerID);
		System.out.println(users.size());
	}

	public static void broadcastMessage(Vertx vertx, String message) {
		HashMap<JsonObject, String> users = getActiveUsers(vertx);
		EventBus bus = vertx.eventBus();

		for (JsonObject key : users.keySet()) {
			System.out.println("Sending message to User " + key + " with handlerID: " + users.get(key));
			bus.send(users.get(key), message);
		}
	}

}
