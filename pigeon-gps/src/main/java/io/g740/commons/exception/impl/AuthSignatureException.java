package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class AuthSignatureException extends BasicException {
    public AuthSignatureException(String errMessage) {
        super(CommonErrorCodeI18N.AUTH_SIGNATURE_ERROR, errMessage);
    }
}
