package io.vertx.webchat.util.auth;

import io.vertx.ext.apex.handler.FormLoginHandler;
import io.vertx.ext.auth.AuthProvider;

public interface FormLoginRememberHandler extends FormLoginHandler {
	/**
	 * The default value of the form attribute which will contain the rememberMe-state
	 */
	static final String DEFAULT_REMEMBERME_PARAM = "rememberMe";
	
	static final String DEFAULT_REDIRECT_URL = "/";

	static final String DEFAULT_EMAIL_PARAM = "email";
	/**
	 * Create a handler
	 *
	 * @param authProvider the auth service to use
	 * @return the handler
	 */
	static FormLoginRememberHandler create(AuthProvider authProvider) {
		return new FormLoginRememberHandlerImpl(authProvider, DEFAULT_EMAIL_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM, DEFAULT_REMEMBERME_PARAM, DEFAULT_REDIRECT_URL);
	}

	/**
	 * Create a handler
	 * 
	 * @param authProvider the auth service to use
	 * @param emailParam the value of the form attribute which will contain
	 *            the email
	 * @param passwordParam the value of the form attribute which will contain
	 *            the password
	 * @param returnURLParam the value of the form attribute which will contain
	 *            the return url
	 * @param rememberMeParam the value of the form attribute which will contain
	 *            the remember-me state
	 * @param defaultRedirectURL a URL that will be used as default return-url, when there is none available in the form
	 * @return the handler
	 */
	static FormLoginRememberHandler create(AuthProvider authProvider, String emailParam, String passwordParam, String returnURLParam, String rememberMeParam, String defaultRedirectURL) {
		return new FormLoginRememberHandlerImpl(authProvider, emailParam, passwordParam, returnURLParam, rememberMeParam, defaultRedirectURL);
	}
}
