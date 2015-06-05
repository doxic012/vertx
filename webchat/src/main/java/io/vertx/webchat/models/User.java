package io.vertx.webchat.models;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * create TABLE user(
 * `uid` int(11) NOT NULL AUTO_INCREMENT,
 * `name` varchar(45) NOT NULL,
 * `email` varchar(128) NOT NULL,
 * `password` varchar(128) DEFAULT NULL,
 * `salt` varchar(128) DEFAULT NULL,
 * `timestamp` timestamp,
 * PRIMARY KEY (`uid`),
 * UNIQUE KEY `email_UNIQUE` (`email`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
 */
@Entity
@Table(name = "user", uniqueConstraints = @UniqueConstraint(columnNames = "uid"))
public class User implements Serializable {

	private static final long serialVersionUID = 5370050875839204057L;

	@Id
	@GeneratedValue
	private Integer uid;

	@Column
	private String name;

	@Column
	private String email;

	@Column
	@JsonIgnore
	private String password;

	@Column
	@JsonIgnore
	private String salt;

	@Column
	@JsonIgnore
	private Timestamp timestamp;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "contact", joinColumns = @JoinColumn(name = "uid"), inverseJoinColumns = @JoinColumn(name = "uidForeign"))
	private List<User> contacts;
	
	public List<User> getContacts() {
		return contacts;
	}

	public void setContacts(List<User> contacts) {
		this.contacts = contacts;
	}

	public User() {
	}

	public String getEmail() {
		return email;
	}

	public Integer getUid() {
		return uid;
	}

	public String getPassword() {
		return password;
	}

	public String getSalt() {
		return salt;
	}

	public String getName() {
		return name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUid(Integer id) {
		this.uid = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JsonObject toJson() {
		JsonObject user = new JsonObject().put("uid", getUid()).put("name", getName()).put("email", getEmail());
		return user;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}
