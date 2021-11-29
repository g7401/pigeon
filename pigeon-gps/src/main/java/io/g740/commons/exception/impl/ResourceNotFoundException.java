package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author : zxiuw
 * @date : 2019-10-14 16:56
 * @description : 资源没找到异常
 */
public class ResourceNotFoundException extends BasicException {
    public ResourceNotFoundException(String errMessage) {
        super(CommonErrorCodeI18N.RESOURCE_NOT_FOUND_ERROR, errMessage);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(CommonErrorCodeI18N.RESOURCE_NOT_FOUND_ERROR, message, cause);
    }
}
