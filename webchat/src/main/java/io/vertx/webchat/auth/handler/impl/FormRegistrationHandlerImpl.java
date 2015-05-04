package io.vertx.webchat.auth.handler.impl;

import io.vertx.core.Handler;
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

public class FormRegistrationHandlerImpl implements FormRegistrationHandler {
	private static final Logger log = LoggerFactory.getLogger(FormRegistrationHandlerImpl.class);

	private final String usernameParam;
	private final String passwordParam;
	private final String emailParam;
	private final String returnURLParam;

	private final boolean loginOnSuccess;

	private final Handler<JsonObject> registrationHandler;
	private final AuthProvider authProvider;

	public FormRegistrationHandlerImpl(Handler<JsonObject> registrationHandler, AuthProvider authProvider, String usernameParam, String emailParam, String passwordParam, String returnURLParam, boolean loginOnSuccess) {
		this.registrationHandler = registrationHandler;
		this.authProvider = authProvider;

		this.usernameParam = usernameParam;
		this.passwordParam = passwordParam;
		this.emailParam = emailParam;

		this.returnURLParam = returnURLParam;
		this.loginOnSuccess = loginOnSuccess;

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

		if (registrationHandler == null) {
			context.fail(new NullPointerException("No registration handler available to invoke"));
			return;
		}

		// Call the registration handler with the registration information
		JsonObject user = new JsonObject().put(usernameParam, username).put(emailParam, email).put(passwordParam, password);
		registrationHandler.handle(user);

		Session session = context.session();
		if (session == null) {
			context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
			return;
		}

		// Mark the registered user as logged in
		if (loginOnSuccess) {
			if (authProvider != null) {
				log.debug("auto login after registration for principal " + username);
				JsonObject principal = new JsonObject().put("username", username);
				session.setPrincipal(principal);
				session.setAuthProvider(authProvider);
			} else {
				log.error("No valid auth-provider - skipping login");
			}
		}

		// Redirecting if possible
		String returnURL = session.remove(returnURLParam);
		if (returnURL == null)
			context.fail(new IllegalStateException("Logged in OK, but no return URL"));
		else
			req.response().putHeader("location", returnURL).setStatusCode(302).end();
	}
}
