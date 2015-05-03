package io.vertx.webchat.auth;


import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

public class SaltedAuthInfo implements SaltedAuthenticationInfo {

	private static final long serialVersionUID = 6919782659456404017L;
	private final String username;
	private final String password;
	private final String salt;

	public SaltedAuthInfo(String username, String password, String salt) {
		this.username = username;
		this.password = password;
		this.salt = salt;
	}

	@Override
	public Object getCredentials() {
		return password;
	}

	@Override
	public ByteSource getCredentialsSalt() {
		return new SimpleByteSource(Base64.decode(salt));
	}

	@Override
	public PrincipalCollection getPrincipals() {
		PrincipalCollection coll = new SimplePrincipalCollection(username, username);
		System.out.println("getting principals"+coll.getPrimaryPrincipal());
		return coll;
	}
}