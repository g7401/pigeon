package io.g740.pigeon.biz.service.handler.oauth;

import io.g740.commons.exception.impl.AuthUnauthorizedException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.object.entity.app.AppDo;
import io.g740.pigeon.biz.object.entity.df.DfDo;
import io.g740.pigeon.biz.persistence.jpa.app.AppDfRepository;
import io.g740.pigeon.biz.persistence.jpa.app.AppRepository;
import io.g740.pigeon.biz.persistence.jpa.app.AppUserRepository;
import io.g740.pigeon.biz.service.interfaces.admin.DfService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author bbottong
 */
@Handler
public class AuthorizationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationHandler.class);

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppDfRepository appDfRepository;

    @Autowired
    private DfService dfService;

    public void authorizeUserRole(UserInfo operationUserInfo, String... allowedRoles) throws ServiceException {
        boolean found = false;
        for (String allowedRole : allowedRoles) {
            if (operationUserInfo.getRoleName().equalsIgnoreCase(allowedRole)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new AuthUnauthorizedException(operationUserInfo.getUsername());
        }
    }

    public void authorizeAppAccessToDf(String appKey, String dfKey) throws ServiceException {
        AppDo appDo = this.appRepository.findByAppKey(appKey);
        if (appDo == null) {
            throw new ResourceNotFoundException(AppDo.RESOURCE_NAME + ":" + appKey);
        }

        DfSimpleDto dfSimpleDto = this.dfService.getDfByKey(dfKey, null);
        if (dfSimpleDto == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }

        boolean exists = this.appDfRepository.existsByAppUidAndDfUid(appDo.getUid(), dfSimpleDto.getDfUid());
        if (!exists) {
            throw new AuthUnauthorizedException(appKey);
        }
    }

    public void authorizeUserAccessToDf(UserInfo userInfo, String dfKey) throws ServiceException {
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(userInfo.getRoleName())) {
            return;
        }

        DfSimpleDto dfSimpleDto = this.dfService.getDfByKey(dfKey, userInfo);
        if (dfSimpleDto == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }

        if (userInfo.getUsername().equalsIgnoreCase(dfSimpleDto.getCreateBy())) {
            return;
        }

        List<Long> listOfAppUid = this.appUserRepository.findByUsername(userInfo.getUsername());
        if (CollectionUtils.isEmpty(listOfAppUid)) {
            throw new AuthUnauthorizedException(userInfo.getUsername());
        }

        List<Long> listOfDfUid = this.appDfRepository.findDfUidByAppUidIn(listOfAppUid);
        if (CollectionUtils.isEmpty(listOfDfUid)) {
            throw new AuthUnauthorizedException(userInfo.getUsername());
        }

        if (!listOfDfUid.contains(dfSimpleDto.getDfUid())) {
            throw new AuthUnauthorizedException(userInfo.getUsername());
        }
    }
}
