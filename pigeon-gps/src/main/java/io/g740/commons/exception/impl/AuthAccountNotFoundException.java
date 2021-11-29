package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class AuthAccountNotFoundException extends BasicException {
    public AuthAccountNotFoundException(String errMessage) {
        super(CommonErrorCodeI18N.AUTH_ACCOUNT_NOT_FOUND_ERROR, errMessage);
    }
}
