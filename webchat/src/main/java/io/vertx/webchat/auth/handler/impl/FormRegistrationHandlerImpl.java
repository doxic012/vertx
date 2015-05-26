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
	private final String roleNames;
	private final AuthProvider authProvider;


	public FormRegistrationHandlerImpl(HashInfo hashInfo, String roleNames, AuthProvider authProvider, String usernameParam, String emailParam, String passwordParam, String returnURLParam, boolean loginOnSuccess, String defaultReturnURL) {
		this.hashInfo = hashInfo;
		this.roleNames = roleNames;
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

		// check form params
		if (username == null || email == null || password == null) {
			context.fail(400);
			return;
		}

		// check hashing information
		if (hashInfo == null) {
			context.fail(new NullPointerException("No hashing information available"));
			return;
		}
		
		// get session
		Session session = context.session();
		if (session == null) {
			context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
			return;
		}
		
		// registration process
		User registeredUser = User.registerUser(hashInfo, username, email, password, roleNames);	

		// Mark the registered user as logged in
		if (loginOnSuccess) {
			if (authProvider != null && registeredUser != null) {
				log.debug("auto login after registration for principal " + username);

				session.setPrincipal(registeredUser.toJson());
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
