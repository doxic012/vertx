package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.webchat.models.Contact;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.HibernateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

public class ContactMapper {
    private static final Logger log = LoggerFactory.getLogger(ContactMapper.class);

    @SuppressWarnings("unchecked")
    public static JsonArray getContacts(int uid) {
        Session session = HibernateUtil.getSession();

        User user = (User) session.createQuery("FROM User u INNER JOIN FETCH u.contacts where u.uid=:uid").setParameter("uid", uid).uniqueResult();

        List<JsonObject> contactList = new ArrayList<>();
        if (user.getContacts() != null) {
            user.getContacts().forEach(contact -> {
                contactList.add(contact.toJson());
            });
        }

        return new JsonArray(contactList);
    }


    // Change -> ContactList hinzugefügt
    public static JsonArray getContactList(int uid) {
        Session session = HibernateUtil.getSession();
        List<Contact> contacts = (List<Contact>) session.createQuery("FROM Contact where u.uid=:uid").setParameter("uid", uid).list();

        return new JsonArray(contacts);
    }

    public static JsonObject addContact(int uid, int uidForeign) {
        Session session = HibernateUtil.getSession();
        session.beginTransaction();

        try {
            Contact contact = new Contact();
            contact.setUid(uid);
            contact.setUidForeign(uidForeign);
            contact.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            contact.setNotified(false);

            session.save(contact);

            return new JsonObject(Json.encode(contact));
        } catch (Exception e) {
            log.debug("Exception at addContact to database: "+e.getMessage());
        } finally {
            session.getTransaction().commit();

            if (session.isOpen())
                session.close();
        }
        return null;
    }

    public static boolean removeContact(int uid, int uidForeign) {
        Session session = HibernateUtil.getSession();
        session.beginTransaction();

        try {
            session.createSQLQuery("DELETE FROM Contact WHERE uid = " + uid + " AND uidForeign = " + uidForeign).executeUpdate();
            return true;
        } catch (Exception e) {
            log.debug("Exception at removeContact from database: "+e.getMessage());
        } finally {
            session.getTransaction().commit();

            if (session.isOpen())
                session.close();
        }

        return false;
    }

    public static boolean hasNotification(int uid, int uidForeign) {
        Session session = HibernateUtil.getSession();
        Contact contact = (Contact) session.createQuery("FROM Contact WHERE uid=:uid and uidForeign=:uidForeign")
                .setParameter("uid", uid)
                .setParameter("uidForeign", uidForeign)
                .setMaxResults(1)
                .uniqueResult();

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

            return true;
        } catch (Exception e) {
            log.debug("Exception at setNotification to database: "+e.getMessage());

        } finally {
            session.getTransaction().commit();

            if (session.isOpen())
                session.close();
        }
        return false;
    }
}
