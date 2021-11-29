package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author : bbottong
 * @date : 2021-02-24 23:10
 * @description : 服务繁忙
 */
public class ServiceBusyException extends BasicException {
    public ServiceBusyException(String errMessage) {
        super(CommonErrorCodeI18N.SERVICE_BUSY_ERROR, errMessage);
    }

    public ServiceBusyException(String message, Throwable cause) {
        super(CommonErrorCodeI18N.SERVICE_BUSY_ERROR, message, cause);
    }
}
