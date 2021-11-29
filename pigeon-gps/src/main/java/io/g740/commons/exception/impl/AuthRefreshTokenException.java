package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class AuthRefreshTokenException extends BasicException {
    public AuthRefreshTokenException(String errMessage) {
        super(CommonErrorCodeI18N.AUTH_REFRESH_TOKEN_ERROR, errMessage);
    }
}
