package io.vertx.webchat.auth.handler.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.webchat.auth.handler.FormRegistrationHandler;
import io.vertx.webchat.auth.hash.HashInfo;
import io.vertx.webchat.hibernate.HibernateUtil;
import io.vertx.webchat.models.User;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class FormRegistrationHandlerImpl implements FormRegistrationHandler {
	private static final Logger log = LoggerFactory.getLogger(FormRegistrationHandlerImpl.class);

	private final String usernameParam;
	private final String passwordParam;
	private final String emailParam;
	private final String returnURLParam;
	private final String defaultReturnURL;

	private final boolean loginOnSuccess;

	private final HashInfo hashInfo;
	private final AuthProvider authProvider;

	/**
	 * Register a new user with a username, email an password.
	 * Optional: Admin role
	 *
	 * @param session
	 * @param username
	 * @param email
	 * @param plainTextPassword
	 * @param isAdmin
	 */
	private User registerUser(HashInfo hashingInfo, String username, String email, String plainTextPassword) {

		ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
		SimpleHash hash = new SimpleHash(hashingInfo.getAlgorithmName(), plainTextPassword, salt, hashingInfo.getIterations());

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setRoleNames("user");

		// Generate hashing-function and encode password
		user.setPassword(hashingInfo.isHexEncoded() ? hash.toHex() : hash.toBase64());
		user.setSalt(salt.toString());

		System.out.println("User with email:" + user.getEmail() + " hashedPassword:" + user.getPassword() + " salt:" + user.getSalt());

		return user;
	}

	public FormRegistrationHandlerImpl(HashInfo hashInfo, AuthProvider authProvider, String usernameParam, String emailParam, String passwordParam, String returnURLParam, boolean loginOnSuccess, String defaultReturnURL) {
		this.hashInfo = hashInfo;
		this.authProvider = authProvider;

		this.usernameParam = usernameParam;
		this.passwordParam = passwordParam;
		this.emailParam = emailParam;

		this.returnURLParam = returnURLParam;
		this.loginOnSuccess = loginOnSuccess;
		this.defaultReturnURL = defaultReturnURL;
	}

	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest req = context.request();

		// Must be a POST
		if (req.method() != HttpMethod.POST) {
			context.fail(405);
			return;
		}

		if (!req.isExpectMultipart()) {
			throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
		}

		MultiMap params = req.formAttributes();
		String username = params.get(usernameParam);
		String email = params.get(emailParam);
		String password = params.get(passwordParam);

		if (username == null || email == null || password == null) {
			context.fail(400);
			return;
		}

		if (hashInfo == null) {
			context.fail(new NullPointerException("No hashing information available"));
			return;
		}

		org.hibernate.Session connectSession = HibernateUtil.getSessionFactory().openSession();
		connectSession.beginTransaction();
		JsonObject principal = null;
		try {
			User user = registerUser(hashInfo, username, email, password);
			connectSession.save(user);

			principal = user.toJson();
		} finally {
			connectSession.getTransaction().commit();

			if (connectSession.isOpen())
				connectSession.close();
		}

		Session session = context.session();
		if (session == null) {
			context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
			return;
		}

		// Mark the registered user as logged in
		if (loginOnSuccess) {
			if (authProvider != null && principal != null) {
				log.debug("auto login after registration for principal " + username);

				session.setPrincipal(principal);
				session.setAuthProvider(authProvider);
			} else {
				log.error("No valid auth-provider - skipping login");
			}
		}

		String returnURL = params.get(returnURLParam);
		if (returnURL == null)
			returnURL = session.remove(returnURLParam);
		if (returnURL == null)
			returnURL = defaultReturnURL;

		req.response().putHeader("location", returnURL).setStatusCode(302).end();
	}
}
