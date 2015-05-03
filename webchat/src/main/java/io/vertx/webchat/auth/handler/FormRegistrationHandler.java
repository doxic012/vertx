package io.vertx.webchat.auth.handler;

import io.vertx.ext.apex.handler.FormLoginHandler;
import io.vertx.ext.apex.handler.impl.FormLoginHandlerImpl;
import io.vertx.ext.auth.AuthProvider;

public interface FormRegistrationHandler extends FormLoginHandler {

	/**
	 * Create a handler
	 *
	 * @param authProvider the auth service to use
	 * @return the handler
	 */
	static FormRegistrationHandler create(AuthProvider authProvider) {
		return new FormRegistrationHandlerImpl(authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM);
	}

	/**
	 * Create a handler
	 * 
	 * @param authProvider the auth service to use
	 * @param usernameParam the value of the form attribute which will contain
	 *            the username
	 * @param passwordParam the value of the form attribute which will contain
	 *            the password
	 * @param returnURLParam the value of the form attribute which will contain
	 *            the return url
	 *
	 * @return the handler
	 */
	static FormRegistrationHandler create(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam) {
		return new FormRegistrationHandlerImpl(authProvider, usernameParam, passwordParam, returnURLParam);
	}
}
