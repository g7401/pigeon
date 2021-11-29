package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class AuthGrantTypeException extends BasicException {
    public AuthGrantTypeException(String errMessage) {
        super(CommonErrorCodeI18N.AUTH_GRANT_TYPE_ERROR, errMessage);
    }
}
