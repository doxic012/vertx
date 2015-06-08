package io.vertx.webchat.util.auth;

import io.vertx.ext.apex.handler.FormLoginHandler;
import io.vertx.ext.auth.AuthProvider;

public interface FormRegistrationHandler extends FormLoginHandler {

	static final String DEFAULT_EMAIL_PARAM = "email";

	static final String DEFAULT_REDIRECT_URL = "/";
	
	static final boolean DEFAULT_LOGIN_BEHAVIOUR = true;
	
	/**
	 * Create a registration handler
	 *
	 * @param registrationHandler the handler that contains the actual registration method. It will be invoked with a
	 *            JSON-object containing all information about the owner passed in the registration-form.
	 * @return the handler
	 */
	static FormRegistrationHandler create(HashInfo hashInfo) {
		return new FormRegistrationHandlerImpl(hashInfo, null, DEFAULT_USERNAME_PARAM, DEFAULT_EMAIL_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, false, DEFAULT_REDIRECT_URL);
	}

	/**
	 * Create a registration handler
	 *
	 * @param registrationHandler the handler that contains the actual registration method. It will be invoked with a
	 *            JSON-object containing all information about the owner passed in the registration-form. The given
	 *            information are mapped to a JsonObject.
	 * @return the handler
	 */
	static FormRegistrationHandler create(HashInfo hashInfo, AuthProvider authProvider) {
		return new FormRegistrationHandlerImpl(hashInfo, authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_EMAIL_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, DEFAULT_LOGIN_BEHAVIOUR, DEFAULT_REDIRECT_URL);
	}

	/**
	 * Create a registration handler
	 *
	 * @param registrationHandler the handler that contains the actual registration method. It will be invoked with a
	 *            JSON-object containing all information about the owner passed in the registration-form. The given
	 *            information are mapped to a JsonObject and, if the owner wishes to auto-login, the information will be
	 *            passed as the active principle to the current session.
	 * @param loginOnSuccess Do a login after a successful registration
	 * @return the handler
	 */

	static FormRegistrationHandler create(HashInfo hashInfo, AuthProvider authProvider, boolean loginOnSuccess, String redirectUrl) {
		return new FormRegistrationHandlerImpl(hashInfo, authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_EMAIL_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, loginOnSuccess, redirectUrl);
	}

	/**
	 * Create a handler
	 * 
	 * @param registrationHandler the handler that contains the actual registration method. It will be invoked with a
	 *            JSON-object containing all information about the owner passed in the registration-form. The given
	 *            information are mapped to a JsonObject and, if the owner wishes to auto-login, the information will be
	 *            passed as the active principle to the current session.
	 * @param usernameParam the value of the form attribute which will contain the username
	 * @param emailParam the value of the form attribute which will contain the email
	 * @param passwordParam the value of the form attribute which will contain the password
	 * @param returnURLParam the value of the form attribute which will contain the return url
	 * @param loginOnSuccess Do a login after a successful registration.
	 * @return the handler
	 */
	static FormRegistrationHandler create(HashInfo hashInfo, String usernameParam, String emailParam, String passwordParam, String returnURLParam, AuthProvider authProvider, boolean loginOnSuccess, String redirectUrl) {
		return new FormRegistrationHandlerImpl(hashInfo, authProvider, usernameParam, passwordParam, emailParam, returnURLParam, loginOnSuccess, redirectUrl);
	}
}
