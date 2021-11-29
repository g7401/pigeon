package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author : zxiuw
 * @date : 2019-10-14 16:56
 * @description : 资源冲突异常
 */
public class ResourceConflictException extends BasicException {
    public ResourceConflictException(String errMessage) {
        super(CommonErrorCodeI18N.RESOURCE_CONFLICT_ERROR, errMessage);
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(CommonErrorCodeI18N.RESOURCE_CONFLICT_ERROR, message, cause);
    }
}
