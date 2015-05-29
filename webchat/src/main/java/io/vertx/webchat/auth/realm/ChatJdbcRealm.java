package io.vertx.webchat.auth.realm;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.mapper.UserMapper;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;
import org.hibernate.SessionFactory;

public class ChatJdbcRealm extends JdbcRealm {

	private static final Logger log = LoggerFactory.getLogger(ChatJdbcRealm.class);

	private SessionFactory sessionFactory = null;
	private HashInfo hashingInfo = null;

	/**
	 * This constructor is based on a {@link HashedCredentialsMatcher} and needs
	 * additional
	 * information about the hashing-algorithm, the iterations, etc.
	 * 
	 * @param factory The hibernate session-factory
	 * @param hashingInfo The hashing information
	 */
	public ChatJdbcRealm(SessionFactory factory, HashInfo hashingInfo) {
		HashedCredentialsMatcher match = new HashedCredentialsMatcher();
		match.setHashAlgorithmName(hashingInfo.getAlgorithmName());
		match.setHashIterations(hashingInfo.getIterations());
		match.setStoredCredentialsHexEncoded(hashingInfo.isHexEncoded());

		this.setCredentialsMatcher(match);
		this.sessionFactory = factory;
		this.hashingInfo = hashingInfo;
	}

	/**
	 * This constructor uses a simpleCredentialMatcher that does not implement
	 * any salted password
	 * comparison
	 * 
	 * @param factory The hibernate session-factory
	 */
	public ChatJdbcRealm(SessionFactory factory) {
		this.sessionFactory = factory;
	}

	private ByteSource getHashedSalt(String salt) {
		return hashingInfo.isHexEncoded() ? new SimpleByteSource(Hex.decode(salt)) : new SimpleByteSource(Base64.decode(salt));
	}

	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
			throws AuthenticationException {

		UsernamePasswordToken userPassToken = (UsernamePasswordToken) token;

		if (userPassToken.getUsername() == null)
			throw new AuthenticationException("Invalid AuthenticationToken: username is null");

		// Open hibernate session and read user credentials

		JsonObject user = UserMapper.getUserByEmail(userPassToken.getUsername());

		if (user == null)
			throw new AuthenticationException("No account found for user '" + userPassToken.getUsername() + "'");

		return new SimpleAuthenticationInfo(user.getString("username"), user.getString("password").toCharArray(), getHashedSalt(user.getString("salt")), user.getString("username"));

	}
}
