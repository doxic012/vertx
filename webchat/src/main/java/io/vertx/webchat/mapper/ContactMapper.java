package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.webchat.models.Contact;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.HibernateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
public class ContactMapper {

	@SuppressWarnings("unchecked")
	public static JsonArray getContacts(int uid) {
		Session connectSession = HibernateUtil.getSession();

		System.out.println("getting contacts for uid: "+uid);
		try {
			List<User> contactList = (List<User>) connectSession.createSQLQuery("SELECT u.* FROM User u INNER JOIN Contact c ON u.uid = c.uidForeign WHERE c.uid=:userId").setParameter("userId", uid).list();		
			return new JsonArray(contactList);
		} catch(NullPointerException e) {
		}

		return new JsonArray();
	}

	public static boolean addContact(int uid, int uidForeign) {
		Session session = HibernateUtil.getSession();
		session.beginTransaction();

		try {
			Contact contact = new Contact();
			contact.setUid(uid);
			contact.setUidForeign(uidForeign);
			contact.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
			contact.setNotified(false);

			session.save(contact);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Wasn't able to add contact to database");
			return false;
		} finally {
			session.getTransaction().commit();

			if (session.isOpen())
				session.close();
		}
		return true;
	}

	public static boolean removeContact(int uid, int uidForeign) {
		Session session = HibernateUtil.getSession();
		session.beginTransaction();

		try {
			session.createSQLQuery("DELETE FROM Contact WHERE uid = " + uid + " AND uidForeign = " + uidForeign).executeUpdate();
		} catch (Exception e) {

			e.printStackTrace();
			System.out.println("Wasn't able to delete contact from database");
			return false;
		} finally {
			session.getTransaction().commit();

			if (session.isOpen())
				session.close();
		}

		return true;
	}

	public static boolean hasNotification(int uid, int uidForeign) {
		Session session = HibernateUtil.getSession();
		Contact contact = (Contact) session.createQuery("FROM Contact WHERE uid=:uid").setParameter("uid", uid).uniqueResult();

		return contact.notified();
	}

	public static boolean setNotification(int uid, int uidForeign) {
		Session session = HibernateUtil.getSession();
		session.beginTransaction();

		try {
			session.createSQLQuery("UPDATE Contact SET notification = true WHERE uid = " + uid + " AND uidForeign = " + uidForeign);

		} catch (Exception e) {

			e.printStackTrace();
			System.out.println("Wasn't able to set notification into database");
			return false;
		} finally {
			session.getTransaction().commit();

			if (session.isOpen())
				session.close();
		}
		return true;
	}
}
