package io.vertx.webchat.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public final class HibernateUtil {

	private static SessionFactory sessionFactory = null;
	private static Session currentSession = null;

	static {
		try {
			System.out.println("Configuring hibernate session factory");
			Configuration config = new Configuration().configure("hibernate.cfg.xml");
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
			sessionFactory = config.buildSessionFactory(serviceRegistry);
			currentSession = sessionFactory.openSession();

		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * Get an existing hibernate Session
	 * 
	 * @return a Session
	 */
	public static Session getSession() {
		
		// TODO: fall: dirty überprüfen
		if (currentSession == null || !currentSession.isOpen())
			currentSession = sessionFactory.openSession();
			
		return currentSession;
	}
}
