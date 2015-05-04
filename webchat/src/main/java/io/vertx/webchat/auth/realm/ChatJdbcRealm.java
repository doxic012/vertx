package io.vertx.webchat.auth.realm;

import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.models.User;

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
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class ChatJdbcRealm extends JdbcRealm {

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

		if (userPassToken.getUsername() == null) {
			System.out.println("Username is null.");
			return null;
		}

		// Open hibernate session and read user credentials
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			User user = User.getUserByUsername(session, userPassToken.getUsername());

			if (user == null) {
				System.out.println("No account found for user '" + userPassToken.getUsername() + "'");
				return null;
			}

			System.out.println("found user: " + user.getUsername() + ", mail: " + user.getEmail() + ", pw: " + user.getPassword() + ", salt: " + user.getSalt());
			System.out.println(hashingInfo);
			return new SimpleAuthenticationInfo(user.getUsername(), user.getPassword().toCharArray(), getHashedSalt(user.getSalt()), user.getUsername());
		} finally {
			session.getTransaction().commit();
			if (session.isOpen())
				session.close();
		}
	}
}
