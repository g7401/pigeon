package io.g740.commons.exception.impl;

import io.g740.commons.exception.BasicException;

import static io.g740.commons.exception.impl.CommonErrorCodeI18N.FILE_DOWNLOAD_ERROR;


/**
 * @author : zxiuwu
 * @version : V1.0
 * @function :
 * @date : 2019/10/14 13:53
 * @description :
 */
public class FileDownloadException extends BasicException {
    public FileDownloadException(String errMessage) {
        super(FILE_DOWNLOAD_ERROR, errMessage);
    }

    public FileDownloadException(String errMessage, Throwable e) {
        super(FILE_DOWNLOAD_ERROR, errMessage, e);
    }
}
