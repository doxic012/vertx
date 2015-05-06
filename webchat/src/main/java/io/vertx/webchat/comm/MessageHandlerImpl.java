package io.vertx.webchat.comm;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

public class MessageHandlerImpl {
	private static String WEBCHAT_USERS_ONLINE = "webchat.users.online";

	private HashMap<JsonObject, String> userMap = new HashMap<JsonObject, String>();

	private Vertx vertx;

	public MessageHandlerImpl(Vertx vertx) {
		this.vertx = vertx;
	}

	/**
	 * Get a Map consisting of N-entries (<String, String>) with UserID,
	 * textHandlerID for the eventBus
	 * 
	 * @param vertx
	 * @return
	 */
	public HashMap<JsonObject, String> getActiveUsers(Vertx vertx) {
		// SharedData data = vertx.sharedData();
		return userMap;// data.<String, String>getLocalMap(WEBCHAT_USERS_ONLINE);
	}

	public void addActiveUser(JsonObject principal, String textHandlerID) {
		HashMap<JsonObject, String> users = getActiveUsers(vertx);

		System.out.println("adding user " + textHandlerID);
		users.put(principal, textHandlerID);
		System.out.println(users.size());
	}

	public void broadcastMessage(String senderId, String message) {
		HashMap<JsonObject, String> users = getActiveUsers(vertx);
		EventBus bus = vertx.eventBus();

		// publish message to everyone except for sender
		for (JsonObject user : users.keySet()) {
			if (users.get(user) != senderId) {
				System.out.println("Sending message to User " + user + " with handlerID: " + users.get(user));
				bus.publish(users.get(user), String.join(";", senderId, message));
			}
		}
	}

}
