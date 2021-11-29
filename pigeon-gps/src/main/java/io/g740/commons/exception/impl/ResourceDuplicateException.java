package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicException;

/**
 * @author : zxiuwu
 * @version : V1.0
 * @function :
 * @date : 2019/10/14 13:53
 * @description : 资源重复异常
 */
public class ResourceDuplicateException extends BasicException {

    public ResourceDuplicateException(String message) {
        super(CommonErrorCodeI18N.RESOURCE_DUPLICATE_ERROR, message);
    }

    public ResourceDuplicateException(String message, Throwable cause) {
        super(CommonErrorCodeI18N.RESOURCE_DUPLICATE_ERROR,message, cause);
    }
}
