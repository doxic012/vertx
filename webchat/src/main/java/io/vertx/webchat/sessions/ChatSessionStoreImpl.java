package io.vertx.webchat.sessions;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.sstore.impl.LocalSessionStoreImpl;

import java.util.Collection;

public class ChatSessionStoreImpl extends LocalSessionStoreImpl implements ChatSessionStore {

	public ChatSessionStoreImpl(Vertx vertx, String sessionMapName, long reaperPeriod) {
		super(vertx, sessionMapName, reaperPeriod);
	}

	@Override
	public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
		resultHandler.handle(Future.succeededFuture(localMap.get(id)));
	}

	@Override
	public void getChatSessions(Handler<AsyncResult<Collection<Session>>> resultHandler) {
		resultHandler.handle(Future.succeededFuture(localMap.values()));
	}
}
