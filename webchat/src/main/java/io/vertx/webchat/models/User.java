package io.vertx.webchat.models;

import io.vertx.core.json.JsonObject;
import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.hibernate.HibernateUtil;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.hibernate.Session;

@Entity
@Table(name = "user")
public class User implements Serializable {

	private static final long serialVersionUID = 5370050875839204057L;

	@Id
	@GeneratedValue
	private Integer id;

	@Column
	private String username;

	@Column
	private String email;

	@Column
	private String roleNames;

	@Column
	private String password;

	@Column
	private String salt;

	public User() {
	}

	public String getEmail() {
		return email;
	}

	public Integer getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public String getSalt() {
		return salt;
	}

	public String getUsername() {
		return username;
	}

	public String getRoleName() {
		return roleNames;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setRoleNames(String roleNames) {
		this.roleNames = roleNames;
	}

	public JsonObject toJson() {
		JsonObject user = new JsonObject().put("name", getUsername()).put("email", getEmail());

		return user;
	}

	public static User getUserByEmail(Session session, String email) {
		System.out.println("getting user by email: " + email);
		return (User) session.createQuery("from User where email=:email").setParameter("email", email).uniqueResult();
	}

	public static User getUserByUsername(String username) {
		Session session = HibernateUtil.getSessionFactory().openSession();

		System.out.println("getting user by username: " + username);
		return (User) session.createQuery("from User where username=:username").setParameter("username", username).uniqueResult();
	}

	public static User getUser(String param) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		return (User) session.createQuery("from User where username=:param or email=:param").setParameter("param", param).uniqueResult();
	}

	public static boolean getUserExists(String param) {
		return getUser(param) != null;
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
	public static User registerUser(HashInfo hashingInfo, String username, String email, String plainTextPassword, String roleNames) {
		User user = null;
		Session connectSession = HibernateUtil.getSessionFactory().openSession();
		connectSession.beginTransaction();

		try {
			ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
			SimpleHash hash = new SimpleHash(hashingInfo.getAlgorithmName(), plainTextPassword, salt, hashingInfo.getIterations());

			// Generate hashing-function and encode password
			user = new User();
			user.setUsername(username);
			user.setEmail(email);
			user.setRoleNames(roleNames);
			user.setPassword(hashingInfo.isHexEncoded() ? hash.toHex() : hash.toBase64());
			user.setSalt(salt.toString());

			System.out.println("User with email:" + user.getEmail() + " hashedPassword:" + user.getPassword() + " salt:" + user.getSalt());

			connectSession.save(user);

		} finally {
			connectSession.getTransaction().commit();

			if (connectSession.isOpen())
				connectSession.close();
		}

		return user;
	}

	public String toString() {
		return "id: " + getId() + ", nick: " + getUsername() + ", email: " + getEmail();
	}
}
