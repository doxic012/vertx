package io.vertx.webchat.auth.realm;

import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.auth.shiro.impl.ShiroAuthRealmBase;
import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.models.User;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.hibernate.SessionFactory;

public class ChatAuthRealm extends ShiroAuthRealmBase {

	private static final Logger log = LoggerFactory.getLogger(ChatAuthRealm.class);

	/**
	 * This authentication-realm is based on the {@link ShiroAuthRealmBase}
	 * using a {@link Subject} to
	 * login the current user.
	 * The login-method is overriden to make use of the implemented
	 * "rememberMe"-status of a UsernamePasswordToken.
	 * 
	 * This {@link ChatAuthRealm} adapts a {@link ChatJdbcRealm} that uses a
	 * {@link HashedCredentialsMatcher}.
	 * 
	 * @param factory A hibernate session-factory
	 * @param hashingInfo Information about a hashing-algorithm that shall be
	 *            used when decoding the user credentials
	 */
	public ChatAuthRealm(SessionFactory factory, HashInfo hashingInfo) {
		ChatJdbcRealm realm = new ChatJdbcRealm(factory, hashingInfo);
		this.realm = realm;
		this.securityManager = new DefaultSecurityManager(realm);
	}

	/**
	 * This authentication-realm is based on the {@link ShiroAuthRealmBase}
	 * using a {@link Subject} to
	 * login the current user.
	 * The login-method is overriden to make use of the implemented
	 * "rememberMe"-status of a UsernamePasswordToken.
	 * 
	 * This {@link ChatAuthRealm} adapts a {@link ChatJdbcRealm} that uses a
	 * {@link SimpleCredentialsMatcher}.
	 * 
	 * @param factory A hibernate session-factory
	 */
	public ChatAuthRealm(SessionFactory factory) {
		ChatJdbcRealm realm = new ChatJdbcRealm(factory);
		this.realm = realm;
		this.securityManager = new DefaultSecurityManager(realm);
	}

	
	/**
	 * This method is used to login a principal with given credentials
	 * The login-method is overriden to make use of the implemented "rememberMe"-status of a UsernamePasswordToken,
	 * passed as a boolean "rememberMe" inside the credentials-{@link JsonObject}.
	 */
	@Override
	public void login(JsonObject principal, JsonObject credentials) {
		SubjectContext subjectContext = new DefaultSubjectContext();
		Subject subject = securityManager.createSubject(subjectContext);
		String username = principal.getString("username");
		String password = credentials.getString("password");
		boolean rememberMe = credentials.getBoolean("rememberMe");

		AuthenticationToken token = new UsernamePasswordToken(username, password, rememberMe);

		try {
			log.debug(String.format("Trying to login (username: %s, password: %s, rememberMe: %s", username, password, rememberMe));
			subject.login(token);
		} catch (AuthenticationException e) {
			throw new VertxException(e);
		}
	}
	
	@Override
	public boolean hasRole(JsonObject principal, String role) {
		if(principal == null || role.isEmpty())
			return false;
		
		return principal.getString("roles").contains(role);
	}
}
