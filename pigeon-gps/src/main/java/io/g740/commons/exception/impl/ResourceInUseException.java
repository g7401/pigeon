package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicException;

/**
 * @author : zxiuwu
 * @version : V1.0
 * @function :
 * @date : 2019/10/14 13:53
 * @description : 资源已占用异常
 */
public class ResourceInUseException extends BasicException {
    public ResourceInUseException(String message) {
        super(CommonErrorCodeI18N.RESOURCE_IN_USE_ERROR, message);
    }

    public ResourceInUseException(String message, Throwable cause) {
        super(CommonErrorCodeI18N.RESOURCE_IN_USE_ERROR, message, cause);
    }
}
