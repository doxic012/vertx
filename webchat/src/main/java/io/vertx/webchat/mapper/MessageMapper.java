package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.webchat.models.Message;
import io.vertx.webchat.util.HibernateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;

public class MessageMapper {

	@SuppressWarnings("unchecked")
	public static JsonArray getMessages(int uid, int uidForeign, int countMessages) {
		Session session = HibernateUtil.getSession();
		System.out.println("getting messages by uid: " + uid + " and uid_foreign: " + uidForeign);

		List<Message> messages = (List<Message>) session.createQuery("FROM Message WHERE (uid=:uid AND uidForeign=:uidForeign) or (uid=:uidForeign AND uidForeign=:uid) ORDER BY id DESC").setMaxResults(countMessages).setParameter("uid", uid).setParameter("uidForeign", uidForeign).list();

		return new JsonArray(messages);
	}

	// Change: boolean -> Message
	public static JsonObject addMessage(int uid, int uidForeign, String content) {
		Message message = new Message();
		Session session = HibernateUtil.getSession();
		session.beginTransaction();

		try {
			message.setMessage(content);
			message.setUid(uid);
			message.setUidForeign(uidForeign);
			message.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));

			session.save(message);
		} catch (Exception e) {
			System.out.println("Wasn't able to add Message to database");
			return null;
		} finally {
			session.getTransaction().commit();
			
			if(session.isOpen())
				session.close();
		}

		return message.toJson();
	}
}
