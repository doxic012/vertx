package io.vertx.webchat.comm;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

public class MessageHandlerImpl {
	private static String WEBCHAT_USERS_ONLINE = "webchat.users.online";

	private static HashMap<String, JsonObject> userMap = new HashMap<String, JsonObject>();

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
	public HashMap<String, JsonObject> getActiveUsers(Vertx vertx) {
		// SharedData data = vertx.sharedData();
		return userMap;// data.<String, String>getLocalMap(WEBCHAT_USERS_ONLINE);
	}

	public void addActiveUser(JsonObject principal, String textHandlerID) {
		HashMap<String, JsonObject> users = getActiveUsers(vertx);

		System.out.println("adding user " + textHandlerID);
		users.put(textHandlerID, principal);
		System.out.println(users.size());
	}

	public void broadcastMessage(String senderId, String message) {
		HashMap<String, JsonObject> users = getActiveUsers(vertx);
		EventBus bus = vertx.eventBus();

		// publish message to everyone except for sender
		for (String handlerId : users.keySet()) {
			
			if (handlerId != senderId) {
				JsonObject sender = users.get(senderId);
				JsonObject receiver = users.get(handlerId);
				
				// check for same Principal
				if (sender.getString("name") != receiver.getString("name")) {
					System.out.println("Sending message to User " + receiver.getString("name") + " with handlerID: " + handlerId);
					bus.publish(handlerId, message);
				}
				else {
					//send different message type to own user (e.g: message-type: sender)
				}
			}
		}
	}

}
