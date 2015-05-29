package io.vertx.webchat.models;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/*
create TABLE message (
		  `id` int(11) NOT NULL AUTO_INCREMENT,
		  `uid` int(11) NOT NULL,
		  `uidForeign` int(11) NOT NULL,
		  `message` text DEFAULT NULL,
		  `timestamp` timestamp,
		  PRIMARY KEY (`id`),
		  FOREIGN KEY (`uid`) REFERENCES user (`uid`),
		  FOREIGN KEY (`uidForeign`) REFERENCES user (`uid`),
		  UNIQUE KEY `id_unique` (`id`)
		) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
*/

@Entity
@Table(name = "message", uniqueConstraints=@UniqueConstraint(columnNames="id"))
public class Message implements Serializable {

	private static final long serialVersionUID = 7886652396955817504L;

	@Id
	@GeneratedValue
	private int id;

	@Column
	private int uid;

	@Column
	private int uidForeign;

	@Column
	private Timestamp timestamp;

	@Column
	private String message;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUidForeign() {
		return uidForeign;
	}

	public void setUidForeign(int uidForeign) {
		this.uidForeign = uidForeign;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
