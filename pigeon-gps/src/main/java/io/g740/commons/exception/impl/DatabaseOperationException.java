package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicException;

/**
 * @author zxiuw
 * @version V1.0
 * @function
 * @date 2019/10/16 10:31
 * @description
 */
public class DatabaseOperationException extends BasicException {
    public DatabaseOperationException(String message) {
        super(CommonErrorCodeI18N.DATABASE_OPERATION_ERROR, message);
    }
}
