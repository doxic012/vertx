package io.vertx.webchat.comm;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.webchat.entity.User;

public final class MessageHandler {
	private static String WEBCHAT_USERS_ONLINE = "webchat.users.online";

	/**
	 * Get a Map consisting of N-entries (<String, String>) with UserID,
	 * textHandlerID for the eventBus
	 * 
	 * @param vertx
	 * @return
	 */
	public static LocalMap<String, String> getActiveUserList(Vertx vertx) {
		SharedData data = vertx.sharedData();
		return data.<String, String>getLocalMap(WEBCHAT_USERS_ONLINE);
	}

	public static void addActiveUser(Vertx vertx, User user, String textHandlerID) {
		LocalMap<String, String> users = getActiveUserList(vertx);

		System.out.println("adding user " + textHandlerID);
		users.put(user.getNickname(), textHandlerID);
		System.out.println(users.size());
	}

	public static void broadcastMessage(Vertx vertx, String message) {
		LocalMap<String, String> users = getActiveUserList(vertx);
		EventBus bus = vertx.eventBus();

		for (String key : users.keySet()) {
			System.out.println("Sending message to User " + key + " with handlerID: " + users.get(key));
			bus.send(users.get(key), message);
		}
	}

}
