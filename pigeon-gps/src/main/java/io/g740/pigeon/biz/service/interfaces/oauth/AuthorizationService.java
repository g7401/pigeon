package io.g740.pigeon.biz.service.interfaces.oauth;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;

/**
 * @author bbottong
 */
public interface AuthorizationService {
    /**
     * 验证指定用户的Role是否在允许Roles范围中
     *
     * @param operationUserInfo
     * @param allowedRoles
     * @throws ServiceException
     */
    void authorizeUserRole(UserInfo operationUserInfo, String... allowedRoles) throws ServiceException;

    /**
     * 验证指定App能否访问指定Data Facet (DF)
     *
     * @param appKey
     * @param dfKey
     * @throws ServiceException
     */
    void authorizeAppAccessToDf(String appKey, String dfKey) throws ServiceException;

    /**
     * 验证指定用户能否访问指定Data Facet (DF)
     *
     * @param userInfo
     * @param dfKey
     * @throws ServiceException
     */
    void authorizeUserAccessToDf(UserInfo userInfo, String dfKey) throws ServiceException;
}
