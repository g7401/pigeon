package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class RepositoryException extends BasicException {
    public RepositoryException(String message) {
        super(CommonErrorCodeI18N.REPOSITORY_ERROR, message);
    }
}
