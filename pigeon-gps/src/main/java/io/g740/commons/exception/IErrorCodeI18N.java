package io.g740.commons.exception;

/**
 * @author : zxiuwu
 */
public interface IErrorCodeI18N {
    /**
     * 获取error code
     *
     * @return
     */
    Integer getCode();

    /**
     * 获取error code symbol
     * @return
     */
    String getCodeSymbol();
}
