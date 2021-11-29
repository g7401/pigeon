package io.g740.pigeon.biz.service.impl.oauth;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.service.handler.oauth.AuthorizationHandler;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bbottong
 */
@Service
public class AuthorizationServiceImpl implements AuthorizationService {
    @Autowired
    private AuthorizationHandler authorizationHandler;

    @Override
    public void authorizeUserRole(UserInfo operationUserInfo, String... allowedRoles) throws ServiceException {
        this.authorizationHandler.authorizeUserRole(operationUserInfo, allowedRoles);
    }

    @Override
    public void authorizeAppAccessToDf(String appKey, String dfKey) throws ServiceException {
        this.authorizationHandler.authorizeAppAccessToDf(appKey, dfKey);
    }

    @Override
    public void authorizeUserAccessToDf(UserInfo userInfo, String dfKey) throws ServiceException {
       this.authorizationHandler.authorizeUserAccessToDf(userInfo, dfKey);
    }

}
