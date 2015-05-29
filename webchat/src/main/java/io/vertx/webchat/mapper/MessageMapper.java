package io.vertx.webchat.mapper;

import io.vertx.core.json.JsonArray;
import io.vertx.webchat.models.Message;
import io.vertx.webchat.util.HibernateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;

public class MessageMapper {
	public static JsonArray getMessages(int uid, int uidForeign, int countMessages) {
		Session session = HibernateUtil.getSession();
		System.out.println("getting messages by uid: " + uid + " and uid_foreign: " + uidForeign);

		List<Message> messages = (List<Message>) session.createQuery("FROM Message WHERE (uid=:uid AND uidForeign=:uidForeign) or (uid=:uidForeign AND uidForeign=:uid) ORDER BY id DESC").setMaxResults(countMessages).setParameter("uid", uid).setParameter("uidForeign", uidForeign).list();

		return new JsonArray(messages);
	}

	public static boolean addMessage(int uid, int uidForeign, String content) {
		Session session = HibernateUtil.getSession();
		session.beginTransaction();

		try {
			Message message = new Message();
			message.setMessage(content);
			message.setUid(uid);
			message.setUidForeign(uidForeign);
			message.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));

			session.save(message);
		} catch (Exception e) {
			System.out.println("Wasn't able to add Message to database");
			return false;
		} finally {
			session.getTransaction().commit();
			
			if(session.isOpen())
				session.close();
		}

		return true;
	}
}
