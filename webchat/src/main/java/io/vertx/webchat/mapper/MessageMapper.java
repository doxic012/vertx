package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.webchat.models.Message;
import io.vertx.webchat.util.HibernateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;

public class MessageMapper {
    private static final Logger log = LoggerFactory.getLogger(MessageMapper.class);

    @SuppressWarnings("unchecked")
    public static JsonArray getMessages(int uid, int uidForeign, int countMessages) {
        Session session = HibernateUtil.getSession();
        System.out.println("getting messages by uid: " + uid + " and uid_foreign: " + uidForeign);

        try {
            List<Message> messages = (List<Message>) session.createQuery("FROM Message WHERE (uid=:uid AND uidForeign=:uidForeign) or (uid=:uidForeign AND uidForeign=:uid) ORDER BY id DESC").setMaxResults(countMessages).setParameter("uid", uid).setParameter("uidForeign", uidForeign).list();
            return new JsonArray(messages);

        } catch (Exception ex) {
            log.debug("Exception thrown at getMessages from database: " + ex.getMessage());
        }

        return null;
    }

    // Change: boolean -> Message
    public static JsonObject addMessage(int uid, int uidForeign, String content) {
        Session session = HibernateUtil.getSession();
        session.beginTransaction();

        try {
            Message message = new Message();
            message.setMessage(content);
            message.setUid(uid);
            message.setUidForeign(uidForeign);
            message.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));

            session.save(message);
            return new JsonObject(Json.encode(message));
        } catch (Exception e) {
            log.debug("Exception at addMessage to database: " + e.getMessage());
        } finally {
            session.getTransaction().commit();

            if (session.isOpen())
                session.close();
        }

        return null;
    }
}
