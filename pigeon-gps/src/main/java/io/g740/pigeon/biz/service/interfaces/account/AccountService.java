package io.g740.pigeon.biz.service.interfaces.account;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.account.AccountProfileDto;
import io.g740.pigeon.biz.object.dto.account.CreateAccountDto;
import io.g740.pigeon.biz.object.dto.account.UpdateAccountDto;

import java.util.List;

/**
 * @author bbottong
 */
public interface AccountService {
    /**
     * 获取指定用户的account profile
     *
     * @param username
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AccountProfileDto getAccountProfile(String username, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询用户名
     *
     * @param username
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<String> queryUsername(String username, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 创建account
     *
     * @param createAccountDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AccountProfileDto createAccount(CreateAccountDto createAccountDto,
                                    UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新account
     *
     * @param updateAccountDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AccountProfileDto updateAccount(UpdateAccountDto updateAccountDto,
                                    UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除account
     *
     * @param username
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteAccount(String username, UserInfo operationUserInfo) throws ServiceException;
}
