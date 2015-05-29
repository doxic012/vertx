package io.vertx.webchat.mapper;

import java.util.List;

import org.hibernate.Session;

import io.vertx.core.json.JsonArray;
import io.vertx.webchat.hibernate.HibernateUtil;
import io.vertx.webchat.models.Message;

public class MessageMapper {
	public static JsonArray getMessages(int uid, int uidForeign, int countMessages) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		System.out.println("getting messages by uid: " + uid + " and uid_foreign: " + uidForeign);
		List<Message> messages = (List<Message>) session.createQuery("FROM Message WHERE uid=:uid AND uidForeign=:uidForeign ORDER BY id DESC LIMIT " + countMessages).setParameter("uid", uid).setParameter("uidForeign", uidForeign).list();
		
		return new JsonArray(messages);
	}

	public static boolean addMessage(int uid, int uidForeign, String message) {
		return false;
	}
}
