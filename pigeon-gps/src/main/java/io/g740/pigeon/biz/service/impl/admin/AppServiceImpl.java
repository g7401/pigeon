package io.g740.pigeon.biz.service.impl.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.app.*;
import io.g740.pigeon.biz.service.handler.openapi.AppHandler;
import io.g740.pigeon.biz.service.interfaces.admin.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bbottong
 */
@Service
public class AppServiceImpl implements AppService {
    @Autowired
    private AppHandler appHandler;

    @Override
    public String generateAppKey(UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.generateAppKey(operationUserInfo);
    }

    @Override
    public String generateAppSecret(UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.generateAppSecret(operationUserInfo);
    }

    @Override
    public AppDto createApp(CreateAppDto createAppDto, UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.createApp(createAppDto, operationUserInfo);
    }

    @Override
    public AppDto updateApp(UpdateAppDto updateAppDto, UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.updateApp(updateAppDto, operationUserInfo);
    }

    @Override
    public void deleteApp(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.appHandler.deleteApp(uid, operationUserInfo);
    }

    @Override
    public AppDto getApp(Long uid, UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.getApp(uid, operationUserInfo);
    }

    @Override
    public PageResult<AppDto> queryApp(
            String appName, Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.queryApp(appName, pageable, operationUserInfo);
    }

    @Override
    public List<AppDfDto> getAppAccessToDf(Long appUid, UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.getAppAccessToDf(appUid, operationUserInfo);
    }

    @Override
    public void replaceAppAccessToDf(ReplaceAppAccessToDfDto replaceAppAccessToDfDto,
                                     UserInfo operationUserInfo) throws ServiceException {
        this.appHandler.replaceAppAccessToDf(replaceAppAccessToDfDto, operationUserInfo);
    }

    @Override
    public List<String> getAppAccessByUser(Long appUid, UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.getAppAccessByUser(appUid, operationUserInfo);
    }

    @Override
    public void replaceAppAccessByUser(ReplaceAppAccessByUserDto replaceAppAccessByUserDto,
                                       UserInfo operationUserInfo) throws ServiceException {
        this.appHandler.replaceAppAccessByUser(replaceAppAccessByUserDto, operationUserInfo);
    }

    @Override
    public List<Long> getAuthorizedDfOfUser(UserInfo operationUserInfo) throws ServiceException {
        return this.appHandler.getAuthorizedDfOfUser(operationUserInfo);
    }
}
