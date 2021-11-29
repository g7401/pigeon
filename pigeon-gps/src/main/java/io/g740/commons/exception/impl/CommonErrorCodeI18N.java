package io.g740.commons.exception.impl;

import io.g740.commons.exception.IErrorCodeI18N;

/**
 * @author zxiuwu
 *
 */
public enum CommonErrorCodeI18N implements IErrorCodeI18N {
    /**
     * AUTH
     */
    AUTH_AUTHENTICATION_FAILED_ERROR(10110),
    AUTH_APP_KEY_NOT_FOUND_ERROR(10111),
    AUTH_SIGNATURE_ERROR(10112),
    AUTH_GRANT_TYPE_ERROR(10113),
    AUTH_REFRESH_TOKEN_ERROR(10114),
    AUTH_ACCOUNT_NOT_FOUND_ERROR(10115),
    AUTH_UNAUTHORIZED_ERROR(10116),

    /**
     * RESOURCE
     */
    RESOURCE_NOT_FOUND_ERROR(10210),
    RESOURCE_DUPLICATE_ERROR(10211),
    RESOURCE_IN_USE_ERROR(10212),
    RESOURCE_CONFLICT_ERROR(10213),
    RESOURCE_DATA_INTEGRITY_VIOLATION_ERROR(10214),

    /**
     *
     */
    FILE_DOWNLOAD_ERROR(10910),
    FILE_UPLOAD_ERROR(10911),
    SHARED_STORAGE_ERROR(10912),
    DATABASE_OPERATION_ERROR(10913),
    JOB_IN_PROCESS_ERROR(10914),
    OPERATION_NOT_SUPPORT(10915),
    REPOSITORY_ERROR(10916),
    SERVICE_BUSY_ERROR(10917);

    private final int code;

    CommonErrorCodeI18N(int code) {
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
