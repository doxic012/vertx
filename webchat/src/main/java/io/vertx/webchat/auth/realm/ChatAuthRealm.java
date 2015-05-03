package io.vertx.webchat.auth.realm;

import io.vertx.ext.auth.shiro.impl.ShiroAuthRealmBase;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.hibernate.SessionFactory;

public class ChatAuthRealm extends ShiroAuthRealmBase {

	public ChatAuthRealm(SessionFactory factory) {

		ChatJdbcRealm realm = new ChatJdbcRealm(factory);
		this.securityManager = new DefaultSecurityManager(realm);
		this.realm = realm;
	}
}
