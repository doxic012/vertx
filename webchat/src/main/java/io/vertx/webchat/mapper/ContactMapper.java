package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.webchat.models.Contact;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.HibernateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

public class ContactMapper {

    @SuppressWarnings("unchecked")
    public static JsonArray getContacts(int uid) {
        Session connectSession = HibernateUtil.getSession();

        try {
            User user = (User) connectSession.createQuery("FROM User u INNER JOIN FETCH u.contacts where u.uid=:uid").setParameter("uid", uid).uniqueResult();

            List<JsonObject> contactList = new ArrayList<JsonObject>();
            if (user.getContacts() != null) {
                user.getContacts().forEach(contact -> {
                    contactList.add(contact.toJson());
                });
            }

            return new JsonArray(contactList);

        } catch (Exception e) {
            e.printStackTrace();
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

    public static boolean setNotification(int uid, int uidForeign, boolean status) {
        Session session = HibernateUtil.getSession();
        session.beginTransaction();

        try {
//			session.createSQLQuery("UPDATE Contact SET notification = :status WHERE uid = :uid AND uidForeign = :uidForeign").setParameter("status", status).setParameter("uid", uid).setParameter("uidForeign", uidForeign);

            Contact contact = (Contact) session.createQuery("FROM Contact WHERE uid = :uid AND uidForeign = :uidForeign").setParameter("uid", uid).setParameter("uidForeign", uidForeign).uniqueResult();
            contact.setNotified(status);
            session.update(contact);
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
