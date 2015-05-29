package io.vertx.webchat.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/*
create TABLE webchat.contact (
		  `uid` int(11) NOT NULL,
		  `uidForeign` int(11) NOT NULL,
		  `notified` boolean DEFAULT NULL,
		  `timestamp` date,
		  PRIMARY KEY (`uid`, `uidForeign`),
		  FOREIGN KEY (`uid`) REFERENCES user (`uid`),
		  FOREIGN KEY (`uidForeign`) REFERENCES user (`uid`)
		) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
*/

@Table(name="contact")
public class Contact implements Serializable {

	private static final long serialVersionUID = -178828679162640733L;

	@Column
	private int uid;
	
	@Column
	private int uidForeign;
	
	@Column
	private int timestamp;
	
	@Column
	private boolean notified;

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

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public boolean notified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}
}
