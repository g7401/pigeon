package io.g740.pigeon.biz.service.impl.oauth;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.oauth.*;
import io.g740.pigeon.biz.service.handler.oauth.AuthenticationHandler;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bbottong
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private AuthenticationHandler authenticationHandler;

    @Override
    public AccessTokenDto createAccessToken(CreateAccessTokenDto createAccessTokenDto) throws ServiceException {
        return this.authenticationHandler.createAccessToken(createAccessTokenDto);
    }

    @Override
    public RefreshedAccessTokenDto refreshAccessToken(
            RefreshAccessTokenDto refreshAccessTokenDto) throws ServiceException {
        return this.authenticationHandler.refreshAccessToken(refreshAccessTokenDto);
    }

    @Override
    public void authenticateApp(String appKey, String accessToken) throws ServiceException {
        this.authenticationHandler.authenticateApp(appKey, accessToken);
    }

    @Override
    public SignedInDto signIn(SignInDto signInDto) throws ServiceException {
        return this.authenticationHandler.signIn(signInDto);
    }

    @Override
    public void signOut(String username) throws ServiceException {
        this.authenticationHandler.signOut(username);
    }

    @Override
    public void authenticateUser(String username, String accessToken) throws ServiceException {
        this.authenticationHandler.authenticateUser(username, accessToken);
    }

    @Override
    public String validateAccessTokenAndExtractUsername(String accessToken) throws ServiceException {
        return this.authenticationHandler.validateAccessTokenAndExtractUsername(accessToken);
    }

    @Override
    public UserInfo validateAccessTokenAndRetrieveUserInfo(String accessToken) throws ServiceException {
        return this.authenticationHandler.validateAccessTokenAndRetrieveoperationUserInfo(accessToken);
    }


}
