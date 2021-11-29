package io.g740.pigeon.biz.service.interfaces.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.app.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author bbottong
 * <p>
 * App相关服务
 */
public interface AppService {
    /**
     * 生成App Key
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    String generateAppKey(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 生成App Secret
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    String generateAppSecret(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 创建App
     *
     * @param createAppDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AppDto createApp(CreateAppDto createAppDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新指定App
     *
     * @param updateAppDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AppDto updateApp(UpdateAppDto updateAppDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除指定App
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteApp(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取指定App
     *
     * @param uid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AppDto getApp(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询App
     *
     * @param appName
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    PageResult<AppDto> queryApp(
            String appName, Pageable pageable, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取指定App能访问哪些Data Facets的配置
     *
     * @param appUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<AppDfDto> getAppAccessToDf(Long appUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新指定App能访问哪些Data Facets的配置
     *
     * @param replaceAppAccessToDfDto
     * @param operationUserInfo
     * @throws ServiceException
     */
    void replaceAppAccessToDf(ReplaceAppAccessToDfDto replaceAppAccessToDfDto,
                              UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取指定App能被哪些用户访问的配置
     *
     * @param appUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<String> getAppAccessByUser(Long appUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新指定App能被哪些用户访问的配置
     *
     * @param replaceAppAccessByUserDto
     * @param operationUserInfo
     * @throws ServiceException
     */
    void replaceAppAccessByUser(ReplaceAppAccessByUserDto replaceAppAccessByUserDto,
                                UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出指定用户能访问哪些Data Facets
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<Long> getAuthorizedDfOfUser(UserInfo operationUserInfo) throws ServiceException;
}
