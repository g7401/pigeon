package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicErrorCodeI18N;
import io.g740.commons.exception.BasicException;


/**
 * @author zxiuw
 */
public class ServiceException extends BasicException {
    public ServiceException(String message) {
        super(BasicErrorCodeI18N.UNKNOWN_ERROR, message);
    }

    public ServiceException(String message, Throwable throwable) {
        super(BasicErrorCodeI18N.UNKNOWN_ERROR, message, throwable);
    }
}
