package io.vertx.webchat.models;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/*
  create TABLE webchat.user(
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `email` varchar(128) NOT NULL,
  `password` varchar(128) DEFAULT NULL,
  `salt` varchar(128) DEFAULT NULL,
  `timestamp` timestamp,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

 */
@Entity
@Table(name = "user")
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
	private String password;

	@Column
	private String salt;

	@Column
	private Timestamp timestamp;
	
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
		JsonObject user = new JsonObject()
			.put("uid", getUid())
			.put("name", getName())
			.put("email", getEmail())
			.put("timestamp", getTimestamp().toString());
		return user;
	}


	public String toString() {
		return "id: " + getUid() + ", nick: " + getName() + ", email: " + getEmail();
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}
