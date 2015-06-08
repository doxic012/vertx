package io.vertx.webchat.util.auth.realm;

import io.vertx.webchat.mapper.UserMapper;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.auth.HashInfo;

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

public class ChatJdbcRealm extends JdbcRealm {

	private HashInfo hashingInfo = null;

	/**
	 * This constructor is based on a {@link HashedCredentialsMatcher} and needs
	 * additional
	 * information about the hashing-algorithm, the iterations, etc.
	 * 
	 * @param factory The hibernate session-factory
	 * @param hashingInfo The hashing information
	 */
	public ChatJdbcRealm(HashInfo hashingInfo) {
		HashedCredentialsMatcher match = new HashedCredentialsMatcher();
		match.setHashAlgorithmName(hashingInfo.getAlgorithmName());
		match.setHashIterations(hashingInfo.getIterations());
		match.setStoredCredentialsHexEncoded(hashingInfo.isHexEncoded());

		this.setCredentialsMatcher(match);
		this.hashingInfo = hashingInfo;
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

		// Open hibernate session and read owner credentials

		User user = UserMapper.getUserCredentials(userPassToken.getUsername());

		System.out.println(user);
		if (user == null)
			throw new AuthenticationException("No account found for owner '" + userPassToken.getUsername() + "'");

		return new SimpleAuthenticationInfo(user.getName(), user.getPassword().toCharArray(), getHashedSalt(user.getSalt()), user.getName());

	}
}
