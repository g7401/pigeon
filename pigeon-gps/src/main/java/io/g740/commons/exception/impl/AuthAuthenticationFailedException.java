package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class AuthAuthenticationFailedException extends BasicException {
    public AuthAuthenticationFailedException(String errMessage) {
        super(CommonErrorCodeI18N.AUTH_AUTHENTICATION_FAILED_ERROR, errMessage);
    }
}
