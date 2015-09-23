package io.vertx.webchat.util.auth.realm;

import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.auth.shiro.impl.ShiroAuthRealmBase;
import io.vertx.webchat.util.auth.HashInfo;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.subject.support.DefaultSubjectContext;

public class ChatAuthRealm extends ShiroAuthRealmBase {

	private static final Logger log = LoggerFactory.getLogger(ChatAuthRealm.class);

	/**
	 * This authentication-realm is based on the {@link ShiroAuthRealmBase}
	 * using a {@link Subject} to
	 * login the current owner.
	 * The login-method is overriden to make use of the implemented
	 * "rememberMe"-status of a UsernamePasswordToken.
	 * 
	 * This {@link ChatAuthRealm} adapts a {@link ChatJdbcRealm} that uses a
	 * {@link HashedCredentialsMatcher}.
	 * 
	 * @param hashingInfo Information about a hashing-algorithm that shall be
	 *            used when decoding the owner credentials
	 */
	public ChatAuthRealm(HashInfo hashingInfo) {
		ChatJdbcRealm realm = new ChatJdbcRealm(hashingInfo);
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
		String email = principal.getString("email");
		String password = credentials.getString("password");
		boolean rememberMe = credentials.getBoolean("rememberMe");

		AuthenticationToken token = new UsernamePasswordToken(email, password, rememberMe);

		try {
			log.debug(String.format("Trying to login (email: %s, password: %s, rememberMe: %s", email, password, rememberMe));
			subject.login(token);
		} catch (AuthenticationException e) {
			throw new VertxException(e);
		}
	}
}
