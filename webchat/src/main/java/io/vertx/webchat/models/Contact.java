package io.vertx.webchat.models;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/*
create TABLE contact (
		  `uid` int(11) NOT NULL,
		  `uidForeign` int(11) NOT NULL,
		  `notified` boolean DEFAULT NULL,
		  `timestamp` timestamp,
		  PRIMARY KEY (`uid`, `uidForeign`),
		  FOREIGN KEY (`uid`) REFERENCES user (`uid`),
		  FOREIGN KEY (`uidForeign`) REFERENCES user (`uid`)
		) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
*/

@Entity
@Table(name="contact")
public class Contact implements Serializable {

	private static final long serialVersionUID = -178828679162640733L;

	@Id
	private int uid;

	@Id
	private int uidForeign;
	
	@Column
	private Timestamp timestamp;
	
	@Column
	private boolean notified;

//	@OneToOne(cascade = CascadeType.ALL)
//    @JoinTable (name = "user", joinColumns = {@JoinColumn(name = "uid")}, inverseJoinColumns = {@JoinColumn(name = "uid")})
//	private User contactUser = new User();
//	
//	@OneToOne(cascade = CascadeType.ALL)
//    @JoinTable (name = "user", joinColumns = {@JoinColumn(name = "uidforeign")}, inverseJoinColumns = {@JoinColumn(name = "uid")})
//	private User contactForeign = new User();
//	
//	
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

	public boolean notified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}
}
