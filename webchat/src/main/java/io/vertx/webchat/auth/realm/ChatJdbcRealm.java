package io.vertx.webchat.auth.realm;

import io.vertx.webchat.auth.SaltedAuthInfo;
import io.vertx.webchat.models.User;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class ChatJdbcRealm extends JdbcRealm {

	private SessionFactory sessionFactory = null;

	public ChatJdbcRealm(SessionFactory factory) {
		this.sessionFactory = factory;
	}

	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
			throws AuthenticationException {
		
		UsernamePasswordToken userPassToken = (UsernamePasswordToken) token;

		System.out.println("userpasstoken username: "+userPassToken.getUsername());
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

			System.out.println("found user: "+user.getUsername()+", mail: "+user.getEmail()+", pw: "+user.getPassword());

			// return salted credentials
			return new SaltedAuthInfo(user.getUsername(), user.getPassword(), user.getSalt());
		} finally {
			session.getTransaction().commit();
			if (session.isOpen())
				session.close();
		}
	}
}
