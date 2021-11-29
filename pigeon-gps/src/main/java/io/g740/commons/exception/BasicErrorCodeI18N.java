package io.g740.commons.exception;

/**
 *
 * There are only 3 basic ErrorCode:
 * COLA_ERROR
 * BIZ_ERROR
 * SYS_ERROR
 *
 * @author zxiuwu
 */
public enum BasicErrorCodeI18N implements IErrorCodeI18N {

    UNKNOWN_ERROR(10000),
    IO_ERROR(10001),
    SQL_ERROR(10002),
    ILLEGAL_PARAMETER_ERROR(10003);

    private final Integer code;

    BasicErrorCodeI18N(int code) {
        this.code = code;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getCodeSymbol() {
        return this.name();
    }
}
