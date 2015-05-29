package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.webchat.models.Contact;
import io.vertx.webchat.util.HibernateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;

public class ContactMapper {
	public static JsonArray getContacts(int uid) {
		Session connectSession = HibernateUtil.getSession();
		
		List<Contact> contactList = (List<Contact>) connectSession.createQuery("FROM contact WHERE uid=:uid").setParameter("uid", uid).list();
		return new JsonArray(contactList);
	}
	
	public static boolean addContact(int uid, int uidForeign) {
		Session connectSession = HibernateUtil.getSession();
		connectSession.beginTransaction();
		
		try {
			Contact contact = new Contact();
			contact.setUid(uid);
			contact.setUidForeign(uidForeign);
			contact.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
			contact.setNotified(false);

			connectSession.save(contact);
			
		} catch (Exception e) {
			System.out.println("Wasn't able to add contact to database");
			return false;
		}
		
		connectSession.getTransaction().commit();
		
		return true;
	}
	
	public static boolean removeContact(int uid, int uidForeign) {
		Session connectSession = HibernateUtil.getSession();
		connectSession.beginTransaction();
		
		try {
			connectSession.createSQLQuery("DELETE FROM contact WHERE uid = "+uid+" AND uidForeign = "+uidForeign).executeUpdate();
			connectSession.getTransaction().commit();
		} catch (Exception e) {
			System.out.println("Wasn't able to delete contact from database");
			return false;
		}
		
		return true;
	}
	
	public static boolean hasNotification(int uid, int uidForeign) {
		Session session = HibernateUtil.getSession();
		
		Contact contact = (Contact) session.createQuery("FROM Contact WHERE uid=:uid").setParameter("uid", uid).uniqueResult();
		if(contact.notified()) return true;
		return false;
	}
	
	public static boolean setNotification(int uid, int uidForeign) {
		Session connectSession = HibernateUtil.getSession();
		connectSession.beginTransaction();
		
		try {
			connectSession.createSQLQuery("UPDATE Contact SET notification = true WHERE uid = "+uid+" AND uidForeign = "+uidForeign);
			
		} catch (Exception e) {
			System.out.println("Wasn't able to set notification into database");
			return false;
		}
		
		connectSession.getTransaction().commit();
		
		return true;
	}
}
