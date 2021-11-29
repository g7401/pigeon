package io.g740.commons.exception.impl;


import io.g740.commons.exception.BasicException;

/**
 * @author : Kingzer
 * @date : 2019-10-14 17:06
 * @description : 任务或者job在执行中，不能重复执行
 */
public class JobInProgressException extends BasicException {
    public JobInProgressException(String errMessage) {
        super(CommonErrorCodeI18N.JOB_IN_PROCESS_ERROR, errMessage);
    }
}
