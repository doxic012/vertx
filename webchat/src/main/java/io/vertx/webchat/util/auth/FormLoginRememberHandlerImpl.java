package io.vertx.webchat.util.auth;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.webchat.mapper.UserMapper;

public class FormLoginRememberHandlerImpl implements FormLoginRememberHandler {

	private static final Logger log = LoggerFactory
			.getLogger(FormLoginRememberHandlerImpl.class);

	private final AuthProvider authProvider;
	private final String emailParam;
	private final String passwordParam;
	private final String returnURLParam;
	private final String rememberMeParam;
	private final String defaultReturnURL;

	public FormLoginRememberHandlerImpl(AuthProvider authProvider,
			String emailParam, String passwordParam, String returnURLParam,
			String rememberMeParam, String defaultReturnURL) {
		this.authProvider = authProvider;
		this.emailParam = emailParam;
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
			throw new IllegalStateException(
					"Form body not parsed - do you forget to include a BodyHandler?");
		}

		MultiMap params = req.formAttributes();
		String userMail = params.get(emailParam); // email
		String password = params.get(passwordParam);
		boolean rememberMe = Boolean.parseBoolean(params.get(rememberMeParam));

		if (userMail == null || password == null) {
			context.fail(400);
			return;
		}

		Session session = context.session();
		if (session == null) {
			context.fail(new NullPointerException(
					"No session - did you forget to include a SessionHandler?"));
			return;
		}

		JsonObject principal = new JsonObject().put("email", userMail);
		JsonObject credentials = new JsonObject().put("password", password)
				.put("rememberMe", rememberMe);

		// Authentication-process
		authProvider.login(principal, credentials, res -> {
			log.debug("login invoked, success: " + res.succeeded()
					+ ", principal: " + userMail + ", rememberMe: "
					+ rememberMe);

			if (res.failed()) {
				context.fail(403);
				return;
			}

			// Get principle-data to save in the session
			JsonObject principleData = UserMapper.getUserByEmail(userMail);

			// Mark the owner as logged in
			session.setPrincipal(principleData);
			session.setAuthProvider(authProvider);

			String redirectURL = params.get(returnURLParam);
			if (redirectURL == null)
				redirectURL = session.remove(returnURLParam);
			if (redirectURL == null)
				redirectURL = defaultReturnURL;

			req.response().putHeader("location", redirectURL)
					.setStatusCode(302).end();
			});
	}
}
