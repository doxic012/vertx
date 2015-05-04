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

	public FormLoginRememberHandlerImpl(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam, String rememberMeParam) {
		this.authProvider = authProvider;
		this.usernameParam = usernameParam;
		this.passwordParam = passwordParam;
		this.returnURLParam = returnURLParam;
		this.rememberMeParam = rememberMeParam;
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
		String password = params.get(passwordParam);
		boolean rememberMe = Boolean.parseBoolean(params.get(rememberMeParam));

		if (username == null || password == null) {
			System.out.println("No username or password provided in form - did you forget to include a BodyHandler?");
			context.fail(400);
		} else {
			Session session = context.session();

			if (session == null) {
				context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
			} else {
				JsonObject principal = new JsonObject().put("username", username);
				JsonObject credentials = new JsonObject().put("password", password).put("rememberMe", rememberMe);

				// the general login-process
				authProvider.login(principal, credentials, res -> {
					System.out.println("login process: " + res.succeeded() + ", principal: " + username + "; pw: " + password + ", rememberMe: " + rememberMe);

					if (res.succeeded()) {
						session.setPrincipal(principal);
						session.setAuthProvider(authProvider);
						String returnURL = session.remove(returnURLParam);

						// Redirecting if possible
						if (returnURL == null) {
							context.fail(new IllegalStateException("Logged in OK, but no return URL"));
						} else {
							req.response().putHeader("location", returnURL).setStatusCode(302).end();
						}
					} else {
						context.fail(403); 
					}
				});
			}
		}
	}
}
