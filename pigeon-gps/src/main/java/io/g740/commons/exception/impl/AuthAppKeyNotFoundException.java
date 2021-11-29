package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class AuthAppKeyNotFoundException extends BasicException {
    public AuthAppKeyNotFoundException(String errMessage) {
        super(CommonErrorCodeI18N.AUTH_APP_KEY_NOT_FOUND_ERROR, errMessage);
    }
}
