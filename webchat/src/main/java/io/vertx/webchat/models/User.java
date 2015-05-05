package io.vertx.webchat.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Session;

@Entity
@Table(name="user")
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

	public static User getUserByEmail(Session session, String email) {
		System.out.println("getting user by email: "+email);
		return (User) session.createQuery("from User where email=:email").setParameter("email", email).uniqueResult();
	}

	public static User getUserByUsername(Session session, String username) {
		System.out.println("getting user by username: "+username);
		return (User) session.createQuery("from User where username=:username").setParameter("username", username).uniqueResult();
	}

	public static User getUser(Session session, String param) {
		return (User) session.createQuery("from User where username=:param or email=:param").setParameter("param", param).uniqueResult();
	}
	
	public String toString() {
		return "id: " + getId() + ", nick: " + getUsername() + ", email: " + getEmail();
	}
}
