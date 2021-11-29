package io.g740.pigeon.biz.service.interfaces.oauth;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.oauth.*;

/**
 * @author bbottong
 */
public interface AuthenticationService {
    /**
     * App创建Access Token
     *
     * @param createAccessTokenDto
     * @return
     * @throws ServiceException
     */
    AccessTokenDto createAccessToken(CreateAccessTokenDto createAccessTokenDto) throws ServiceException;

    /**
     * App刷新Refresh Token
     *
     * @param refreshAccessTokenDto
     * @return
     * @throws ServiceException
     */
    RefreshedAccessTokenDto refreshAccessToken(RefreshAccessTokenDto refreshAccessTokenDto) throws ServiceException;

    /**
     * App鉴权
     *
     * @param appKey
     * @param accessToken
     * @throws ServiceException
     */
    void authenticateApp(String appKey, String accessToken) throws ServiceException;

    /**
     * 用户登录
     *
     * @param signInDto
     * @return
     * @throws ServiceException
     */
    SignedInDto signIn(SignInDto signInDto) throws ServiceException;

    /**
     * 用户注销
     *
     * @param username
     * @throws ServiceException
     */
    void signOut(String username) throws ServiceException;

    /**
     * 用户鉴权
     *
     * @param username
     * @param accessToken
     * @throws ServiceException
     */
    void authenticateUser(String username, String accessToken) throws ServiceException;

    /**
     * 验证access token并提取username
     *
     * @param accessToken
     * @return
     * @throws ServiceException
     */
    String validateAccessTokenAndExtractUsername(String accessToken) throws ServiceException;

    /**
     * 验证access token并获取operationUserInfo
     *
     * @param accessToken
     * @return
     * @throws ServiceException
     */
    UserInfo validateAccessTokenAndRetrieveUserInfo(String accessToken) throws ServiceException;
}
