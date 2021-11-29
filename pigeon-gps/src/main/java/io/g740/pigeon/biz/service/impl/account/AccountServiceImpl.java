package io.g740.pigeon.biz.service.impl.account;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.account.AccountProfileDto;
import io.g740.pigeon.biz.object.dto.account.CreateAccountDto;
import io.g740.pigeon.biz.object.dto.account.UpdateAccountDto;
import io.g740.pigeon.biz.service.handler.account.AccountHandler;
import io.g740.pigeon.biz.service.interfaces.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bbottong
 */
@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountHandler accountHandler;

    @Override
    public AccountProfileDto createAccount(CreateAccountDto createAccountDto,
                                           UserInfo operationUserInfo) throws ServiceException {
        return this.accountHandler.createAccount(createAccountDto, operationUserInfo);
    }

    @Override
    public AccountProfileDto updateAccount(UpdateAccountDto updateAccountDto,
                                           UserInfo operationUserInfo) throws ServiceException {
        return this.accountHandler.updateAccount(updateAccountDto, operationUserInfo);
    }

    @Override
    public AccountProfileDto getAccountProfile(String username, UserInfo operationUserInfo) throws ServiceException {
        return this.accountHandler.getAccountProfile(username, operationUserInfo);
    }

    @Override
    public List<String> queryUsername(String username, UserInfo operationUserInfo) throws ServiceException {
        return this.accountHandler.queryUsername(username, operationUserInfo);
    }

    @Override
    public void deleteAccount(String username, UserInfo operationUserInfo) throws ServiceException {
        this.accountHandler.deleteAccount(username, operationUserInfo);
    }
}
