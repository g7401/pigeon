package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicException;

/**
 * @author bbottong
 */
public class ResourceDataIntegrityViolationException extends BasicException {
    public ResourceDataIntegrityViolationException(String message) {
        super(CommonErrorCodeI18N.RESOURCE_DATA_INTEGRITY_VIOLATION_ERROR, message);
    }

    public ResourceDataIntegrityViolationException(String message, Throwable cause) {
        super(CommonErrorCodeI18N.RESOURCE_DATA_INTEGRITY_VIOLATION_ERROR, message, cause);
    }
}
