package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicException;

import static io.g740.commons.exception.impl.CommonErrorCodeI18N.SHARED_STORAGE_ERROR;


/**
 * @author : zxiuwu
 * @version : V1.0
 * @function :
 * @date : 2019/10/14 13:53
 * @description :
 */
public class SharedStorageException extends BasicException {
    public SharedStorageException(String errMessage) {
        super(SHARED_STORAGE_ERROR, errMessage);
    }

    public SharedStorageException(String errMessage, Throwable e) {
        super(SHARED_STORAGE_ERROR, errMessage, e);
    }
}
