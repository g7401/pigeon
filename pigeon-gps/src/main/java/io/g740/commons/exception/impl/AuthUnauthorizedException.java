package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class AuthUnauthorizedException extends BasicException {
    public AuthUnauthorizedException(String errMessage) {
        super(CommonErrorCodeI18N.AUTH_UNAUTHORIZED_ERROR, errMessage);
    }
}
