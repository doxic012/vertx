package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.webchat.models.Contact;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.HibernateUtil;
import io.vertx.webchat.util.auth.HashInfo;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.hibernate.Session;

public class UserMapper extends User {
	public static User getUserCredentials(String email) {
		Session connectSession = HibernateUtil.getSession();
		return (User) connectSession.createQuery("from User where email=:email").setParameter("email", email).uniqueResult();
	}

	public static JsonObject getUserByEmail(String email) {
		Session session = HibernateUtil.getSession();
		User user = (User) session.createQuery("from User where email=:email").setParameter("email", email).uniqueResult();
		return user.toJson();
	}

	@SuppressWarnings("unchecked")
	public static JsonArray getUsers() {
		Session session = HibernateUtil.getSession();

		List<User> userList = (List<User>) session.createQuery("from User").list();
		List<JsonObject> users = new ArrayList<>();
		userList.forEach(user -> {
			users.add(user.toJson());
		});
		return new JsonArray(users);
	}

	public static boolean userExistsByEmail(String email) {
		return getUserByEmail(email) != null;
	}

	/**
	 * Register a new owner with a username, email an password.
	 * Optional: Admin role
	 *
	 * @param hashingInfo the hashing object that contains all information about the hashing algorithm, iterations, etc.
	 * @param username the username
	 * @param email the users email
	 * @param plainTextPassword the plain text password that will be hashed using the hashing information
	 */
	public static JsonObject addUser(HashInfo hashingInfo, String username, String email, String plainTextPassword) {
		JsonObject userObject;
		Session session = HibernateUtil.getSession();
		session.beginTransaction();

		try {
			ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
			SimpleHash hash = new SimpleHash(hashingInfo.getAlgorithmName(), plainTextPassword, salt, hashingInfo.getIterations());

			// Generate hashing-function and encode password
			User user = new User();
			user.setName(username);
			user.setEmail(email);
			user.setPassword(hashingInfo.isHexEncoded() ? hash.toHex() : hash.toBase64());
			user.setSalt(salt.toString());
			user.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));

			System.out.println("User with email:" + user.getEmail() + " hashedPassword:" + user.getPassword() + " salt:" + user.getSalt());

			session.save(user);
			userObject = user.toJson();
		} finally {
			session.getTransaction().commit();
			
			if(session.isOpen())
				session.close();
		}

		return userObject;
	}
}
