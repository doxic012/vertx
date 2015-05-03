package io.vertx.webchat.sessions;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.sstore.SessionStore;

import java.util.Collection;

/**
 * This interface and the proper implementation are an extension to the
 * LocalSessionStore by Tim Fox
 * This SessionStore is extended by a method, providing all entries of the local
 * session map.
 */
public interface ChatSessionStore extends SessionStore {

	/**
	 * Default of how often, in ms, to check for expired sessions
	 */ 
	static final long DEFAULT_REAPER_PERIOD = 1000;

	/**
	 * Default name for map used to store sessions
	 */
	static final String DEFAULT_SESSION_MAP_NAME = "chat.sessions";

	/**
	 * Create a session store
	 *
	 * @param vertx the Vert.x instance
	 * @return the session store
	 */
	static ChatSessionStore create(Vertx vertx) {
		return new ChatSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, DEFAULT_REAPER_PERIOD);
	}

	/**
	 * Create a session store
	 *
	 * @param vertx the Vert.x instance
	 * @param sessionMapName name for map used to store sessions
	 * @return the session store
	 */
	static ChatSessionStore create(Vertx vertx, String sessionMapName) {
		return new ChatSessionStoreImpl(vertx, sessionMapName, DEFAULT_REAPER_PERIOD);
	}

	/**
	 * Create a session store
	 *
	 * @param vertx the Vert.x instance
	 * @param sessionMapName name for map used to store sessions
	 * @param reaperPeriod how often, in ms, to check for expired sessions
	 * @return the session store
	 */
	static ChatSessionStore create(Vertx vertx, String sessionMapName, long reaperPeriod) {
		return new ChatSessionStoreImpl(vertx, sessionMapName, reaperPeriod);
	}

	/**
	 * The extension-method for the SessionStore. A LocalSessionStore doesn't grant access to the
	 * localMap, that contains all active sessions
	 * @param resultHandler
	 */
	public void getChatSessions(Handler<AsyncResult<Collection<Session>>> resultHandler);
}
