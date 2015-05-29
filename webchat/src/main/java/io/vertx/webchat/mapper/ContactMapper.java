package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.webchat.hibernate.HibernateUtil;
import io.vertx.webchat.models.Contact;

import java.util.List;

import org.hibernate.Session;

public class ContactMapper {
	public static JsonArray getContacts(int uid) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		List<Contact> userList = (List<Contact>) session.createQuery("from Contact where uid=:uid").setParameter("uid", uid).list();
		return new JsonArray(userList);
	}
	
	public static boolean addContact(int uid, int uidForeign) {
		return false;
	}
	
	public static boolean removeContact(int uid, int uidForeign) {
		return false;
	}
	
	public static boolean hasNotification(int uid, int uidForeign) {
		return false;
	}
	
	public static boolean setNotification(int uid, int uidForeign) {
		return false;
	}
}
