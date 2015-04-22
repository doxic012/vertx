package io.vertx.webchat.entity;

public class User {
	
	private String nickname;
	private String email;
	private int id = globalIds++;
	private static int globalIds = 0;

	public String getNickname() {
		return nickname;
	}

	public String getEmail() {
		return email;
	}

	public int getId() {
		return id;
	}

	public User(String nickname, String email) {
		this.nickname = nickname+id;
		this.email = email;
	}

	public String toString() {
		return "id: "+id+", nick: "+nickname+", email: "+email;
	}
	// public void setNickname(String nick) {
	// nickname = nick;
	// }
	//
	// public void setEmail(String email) {
	// this.email = email;
	// }
	//
	// public void setId(int id) {
	// this.id = id;
	// }

}
