package io.vertx.webchat.auth.handler;

import io.vertx.ext.apex.handler.FormLoginHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.webchat.auth.handler.impl.FormLoginRememberHandlerImpl;

public interface FormLoginRememberHandler extends FormLoginHandler {
	/**
	 * The default value of the form attribute which will contain the rememberMe-state
	 */
	static final String DEFAULT_REMEMBERME_PARAM = "rememberMe";

	/**
	 * Create a handler
	 *
	 * @param authProvider the auth service to use
	 * @return the handler
	 */
	static FormLoginRememberHandler create(AuthProvider authProvider) {
		return new FormLoginRememberHandlerImpl(authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, DEFAULT_REMEMBERME_PARAM);
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
	 * @param rememberMeParam the value of the form attribute which will contain
	 *            the remember-me state
	 * @return the handler
	 */
	static FormLoginRememberHandler create(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam, String rememberMeParam) {
		return new FormLoginRememberHandlerImpl(authProvider, usernameParam, passwordParam, returnURLParam, rememberMeParam);
	}
}
