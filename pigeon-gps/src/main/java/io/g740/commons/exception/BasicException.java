package io.g740.commons.exception;

/**
 * @author : zxiuwu
 * @version : V1.0
 * @function :
 * @date : 2019/10/14 13:53
 * @description :
 */
public class BasicException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String[] errorParams;

    private IErrorCodeI18N errorCode;

    public BasicException(String errMessage){
        super(errMessage);
        this.errorCode = BasicErrorCodeI18N.UNKNOWN_ERROR;
    }

    public BasicException(IErrorCodeI18N errCode, String errMessage){
        super(errMessage);
        this.errorCode = errCode;
    }

    public BasicException(IErrorCodeI18N errCode, String errMessage, String[] errorParams){
        super(errMessage);
        this.errorCode = errCode;
        this.errorParams = errorParams;
    }

    public BasicException(String errMessage, Throwable e) {
        super(errMessage, e);
        this.errorCode = BasicErrorCodeI18N.UNKNOWN_ERROR;
    }

    public BasicException(IErrorCodeI18N errCode, String errMessage, Throwable e) {
        super(errMessage, e);
        this.errorCode = errCode;
    }

    public String[] getErrorParams() {
        return errorParams;
    }

    public void setErrorParams(String[] errorParams) {
        this.errorParams = errorParams;
    }

    public IErrorCodeI18N getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(IErrorCodeI18N errorCode) {
        this.errorCode = errorCode;
    }
}
