package io.vertx.webchat.auth.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.handler.FormLoginHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.webchat.auth.handler.impl.FormRegistrationHandlerImpl;

public interface FormRegistrationHandler extends FormLoginHandler {

	static final String DEFAULT_EMAIL_PARAM = "email";

	static final boolean DEFAULT_LOGIN_BEHAVIOUR = true;

	/**
	 * Create a registration handler
	 *
	 * @param registrationHandler the handler that contains the actual
	 *            registration method. It will be invoked with a JSON-object
	 *            containing information about the user to register. The
	 *            information will be mapped to the passed form-params
	 * @return the handler
	 */
	static FormRegistrationHandler create(Handler<JsonObject> registrationHandler) {
		return new FormRegistrationHandlerImpl(registrationHandler, null, DEFAULT_USERNAME_PARAM, DEFAULT_EMAIL_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, false);
	}	/**
	 * Create a registration handler
	 *
	 * @param registrationHandler the handler that contains the actual
	 *            registration method. It will be invoked with a JSON-object
	 *            containing information about the user to register. The
	 *            information will be mapped to the passed form-params
	 * @return the handler
	 */
	static FormRegistrationHandler create(Handler<JsonObject> registrationHandler, AuthProvider authProvider) {
		return new FormRegistrationHandlerImpl(registrationHandler, authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_EMAIL_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, DEFAULT_LOGIN_BEHAVIOUR);
	}

	/**
	 * Create a registration handler
	 *
	 * @param registrationHandler the handler that contains the actual
	 *            registration method. It will be invoked with a JSON-object
	 *            containing information about the user to register. The
	 *            information will be mapped to the passed form-params
	 * @param loginOnSuccess Do a login after a successful registration
	 * @return the handler
	 */

	static FormRegistrationHandler create(Handler<JsonObject> registrationHandler, boolean loginOnSuccess, AuthProvider authProvider) {
		return new FormRegistrationHandlerImpl(registrationHandler, authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_EMAIL_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, loginOnSuccess);
	}

	/**
	 * Create a handler
	 * 
	 * @param registrationHandler the handler that contains the actual
	 *            registration method. It will be invoked with a JSON-object
	 *            containing information about the user to register. The
	 *            information will be mapped to the passed form-params
	 * @param usernameParam the value of the form attribute which will contain
	 *            the username
	 * @param passwordParam the value of the form attribute which will contain
	 *            the password
	 * @param returnURLParam the value of the form attribute which will contain
	 *            the return url
	 * @param loginOnSuccess Do a login after a successful registration
	 * @return the handler
	 */
	static FormRegistrationHandler create(Handler<JsonObject> registrationHandler, String usernameParam, String emailParam, String passwordParam, String returnURLParam, boolean loginOnSuccess, AuthProvider authProvider) {
		return new FormRegistrationHandlerImpl(registrationHandler, authProvider, usernameParam, passwordParam, emailParam, returnURLParam, loginOnSuccess);
	}
}
