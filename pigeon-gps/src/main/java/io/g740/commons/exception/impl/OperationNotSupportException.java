package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author : Kingzer
 * @date : 2019-10-14 17:05
 * @description :  不准许的操作异常
 */
public class OperationNotSupportException extends BasicException {
    public OperationNotSupportException(String errMessage) {
        super(CommonErrorCodeI18N.OPERATION_NOT_SUPPORT, errMessage);
    }
}
