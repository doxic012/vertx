package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.hibernate.HibernateUtil;
import io.vertx.webchat.models.User;

import java.util.List;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.hibernate.Session;

public class UserMapper {
	public static JsonObject getUserByEmail(String email) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		System.out.println("getting user by email: " + email);
		User user = (User) session.createQuery("from User where email=:email").setParameter("email", email).uniqueResult();
		
		return user.toJson();
	}

	public static JsonArray getUsers() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		List<User> userList = (List<User>) session.createQuery("from User").list();
		return new JsonArray(userList);
	}
	
	public static boolean userExistsByEmail(String email) {
		return getUserByEmail(email) != null;
	}
	
	/**
	 * Register a new user with a username, email an password.
	 * Optional: Admin role
	 *
	 * @param session
	 * @param username
	 * @param email
	 * @param plainTextPassword
	 * @param isAdmin
	 */
	public static JsonObject addUser(HashInfo hashingInfo, String username, String email, String plainTextPassword) {
		JsonObject userObject;
		Session connectSession = HibernateUtil.getSessionFactory().openSession();
		connectSession.beginTransaction();

		try {
			ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
			SimpleHash hash = new SimpleHash(hashingInfo.getAlgorithmName(), plainTextPassword, salt, hashingInfo.getIterations());

			// Generate hashing-function and encode password
			User user = new User();
			user.setName(username);
			user.setEmail(email);
			user.setPassword(hashingInfo.isHexEncoded() ? hash.toHex() : hash.toBase64());
			user.setSalt(salt.toString());

			System.out.println("User with email:" + user.getEmail() + " hashedPassword:" + user.getPassword() + " salt:" + user.getSalt());

			connectSession.save(user);

			userObject = user.toJson();
		} finally {
			connectSession.getTransaction().commit();

			if (connectSession.isOpen())
				connectSession.close();
		}

		return userObject;
	}
}