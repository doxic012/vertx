package io.vertx.webchat.models;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/*
create TABLE webchat.message (
		  `id` int(11) NOT NULL AUTO_INCREMENT,
		  `uid` int(11) NOT NULL,
		  `uidForeign` int(11) NOT NULL,
		  `message` text DEFAULT NULL,
		  `timestamp` date,
		  PRIMARY KEY (`id`),
		  FOREIGN KEY (`uid`) REFERENCES user (`uid`),
		  FOREIGN KEY (`uidForeign`) REFERENCES user (`uid`),
		  UNIQUE KEY `id_unique` (`id`)
		) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
*/

@Entity
@Table(name = "message")
public class Message implements Serializable {

	private static final long serialVersionUID = 7886652396955817504L;

	@Id
	@GeneratedValue
	private int id;

//	@ManyToOne
//	@JoinTable(name = "user", joinColumns = @JoinColumn(name = "uid"))
	@Column
	private int uid;

//	@ManyToOne
//	@JoinTable(name = "user", joinColumns = @JoinColumn(name = "uid"))
	@Column
	private int uidForeign;

	@Column
	private Date timestamp;

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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDate timestamp) {
		this.timestamp =  Date.valueOf(timestamp);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
