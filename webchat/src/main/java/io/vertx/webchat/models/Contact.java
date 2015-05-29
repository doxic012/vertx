package io.vertx.webchat.models;

public class Contact {
	private int uid;
	private int uidForeign;
	private int timestamp;
	
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
