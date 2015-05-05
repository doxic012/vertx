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
import io.vertx.webchat.auth.handler.FormLoginRememberHandler;

public class FormLoginRememberHandlerImpl implements FormLoginRememberHandler {

	private static final Logger log = LoggerFactory.getLogger(FormLoginRememberHandlerImpl.class);

	private final AuthProvider authProvider;
	private final String usernameParam;
	private final String passwordParam;
	private final String returnURLParam;
	private final String rememberMeParam;
	private final String defaultReturnURL;

	public FormLoginRememberHandlerImpl(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam, String rememberMeParam, String defaultReturnURL) {
		this.authProvider = authProvider;
		this.usernameParam = usernameParam;
		this.passwordParam = passwordParam;
		this.returnURLParam = returnURLParam;
		this.rememberMeParam = rememberMeParam;
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
		String user = params.get(usernameParam); // may be email or username
		String password = params.get(passwordParam);
		boolean rememberMe = Boolean.parseBoolean(params.get(rememberMeParam));

		if (user == null || password == null) {
			context.fail(400);
			return;
		}

		Session session = context.session();
		if (session == null) {
			context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
			return;
		}

		JsonObject principal = new JsonObject().put("username", user);
		JsonObject credentials = new JsonObject().put("password", password).put("rememberMe", rememberMe);

		// Authentication-process
		authProvider.login(principal, credentials, res -> {
			log.debug("login invoked, success: " + res.succeeded() + ", principal: " + user + ", rememberMe: " + rememberMe);

			if (res.failed()) {
				context.fail(403);
				return;
			}

			// Mark the user as logged in
			session.setPrincipal(principal);
			session.setAuthProvider(authProvider);
			String returnURL = session.remove(returnURLParam);

			if (returnURL == null)
				returnURL = defaultReturnURL;
			req.response().putHeader("location", returnURL).setStatusCode(302).end();
		});
	}
}
