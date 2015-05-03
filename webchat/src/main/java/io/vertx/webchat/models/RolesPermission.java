package io.vertx.webchat.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="rolespermission")
public class RolesPermission implements Serializable {

	private static final long serialVersionUID = -1460913250480536136L;

	@Id
	@GeneratedValue
	private Integer id;

	@Column
	private String permission;

	@Column
	private String roleName;

	public RolesPermission() {
	}

	public Integer getId() {
		return id;
	}

	public String getPermission() {
		return permission;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}