package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.HibernateUtil;
import io.vertx.webchat.util.auth.HashInfo;

import java.time.LocalDate;
import java.util.List;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.hibernate.Session;

public class UserMapper {
	public static User getUserCredentials(String email) {
		Session session = HibernateUtil.getSession();
		System.out.println("getting user by email: " + email);
		return (User) session.createQuery("from User where email=:email").setParameter("email", email).uniqueResult();
	}

	public static JsonObject getUserByEmail(String email) {
		Session session = HibernateUtil.getSession();
		System.out.println("getting user by email: " + email);
		User user = (User) session.createQuery("from User where email=:email").setParameter("email", email).uniqueResult();
		return user.toJson();
	}

	public static JsonArray getUsers() {
		Session session = HibernateUtil.getSession();
		
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
		Session connectSession = HibernateUtil.getSession();
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
			user.setTimestamp(LocalDate.now());

			System.out.println("User with email:" + user.getEmail() + " hashedPassword:" + user.getPassword() + " salt:" + user.getSalt());

			connectSession.save(user);

			userObject = user.toJson();
		} finally {
			connectSession.getTransaction().commit();
		}

		return userObject;
	}
}
