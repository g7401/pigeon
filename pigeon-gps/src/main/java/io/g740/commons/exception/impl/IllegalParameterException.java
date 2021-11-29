package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

import static io.g740.commons.exception.BasicErrorCodeI18N.ILLEGAL_PARAMETER_ERROR;

/**
 * @author : Kingzer
 * @date : 2019-10-14 16:57
 * @description : 业务处理中非法或者不合理的参数
 */
public class IllegalParameterException extends BasicException {
    public IllegalParameterException(String errMessage) {
        super(ILLEGAL_PARAMETER_ERROR, errMessage);
    }

    public IllegalParameterException(String errMessage, Throwable cause) {
        super(ILLEGAL_PARAMETER_ERROR, errMessage, cause);
    }
}
