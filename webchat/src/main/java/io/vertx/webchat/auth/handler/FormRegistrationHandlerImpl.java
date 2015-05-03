package io.vertx.webchat.auth.handler;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.webchat.models.User;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;

public class FormRegistrationHandlerImpl implements FormRegistrationHandler {
	private static final Logger log = LoggerFactory.getLogger(FormRegistrationHandlerImpl.class);

	private final AuthProvider authProvider;
	private final String usernameParam;
	private final String passwordParam;
	private final String returnURLParam;

	public FormRegistrationHandlerImpl(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam) {
		this.authProvider = authProvider;
		this.usernameParam = usernameParam;
		this.passwordParam = passwordParam;
		this.returnURLParam = returnURLParam;
	}

	/**
	 * Password generation with Sha256
	 * 
	 * @param user
	 * @param plainTextPassword
	 */
	private void generatePassword(User user, String plainTextPassword) {
		RandomNumberGenerator rng = new SecureRandomNumberGenerator();
		Object salt = rng.nextBytes();

		// Now hash the plain-text password with the random salt and multiple
		// iterations and then Base64-encode the value (requires less space than
		// Hex):
		String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, salt, 1024).toBase64();

		user.setPassword(hashedPasswordBase64);
		user.setSalt(salt.toString());
	}

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
	public void registrate(org.hibernate.Session session, String username, String email, String plainTextPassword, boolean isAdmin) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setRoleNames("");

		if (isAdmin) {
			user.setRoleNames("admin");
		}

		generatePassword(user, plainTextPassword);
		session.save(user);

		System.out.println("User with email:" + user.getEmail() + " hashedPassword:" + user.getPassword() + " salt:" + user.getSalt());

		// create role
		// if (isAdmin) {
		// UserRole role = new UserRole();
		// role.setEmail(email);
		// .setRoleName("admin");
		// session.save(role);
		// }
	}

	// TODO: Registration mit Formular als Handler
	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest req = context.request();
		if (req.method() != HttpMethod.POST) {
			context.fail(405); // Must be a POST
		} else {
			if (!req.isExpectMultipart()) {
				throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
			}

			MultiMap params = req.formAttributes();
			String username = params.get(usernameParam);
			String password = params.get(passwordParam);

			if (username == null || password == null) {
				System.out.println("No username or password provided in form - did you forget to include a BodyHandler?");
				context.fail(400);
			} else {
				Session session = context.session();
				if (session == null) {
					context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
				} else {
					JsonObject principal = new JsonObject().put("username", username);
					authProvider.login(principal, new JsonObject().put("password", password), res -> {
						System.out.println("login process: "+res.succeeded()+", principal: "+principal.getString("username")+"; pw: "+password);
						if (res.succeeded()) {
							session.setPrincipal(principal);
							session.setAuthProvider(authProvider);
							String returnURL = session.remove(returnURLParam);
							
							if (returnURL == null) {
								context.fail(new IllegalStateException("Logged in OK, but no return URL"));
							} else {
								// Now redirect back to the original url
								req.response().putHeader("location", returnURL).setStatusCode(302).end();
							}
						} else {
							context.fail(403); // Failed login
						}
					});
				}

			}
		}
	}
}
