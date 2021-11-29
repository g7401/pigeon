package io.g740.pigeon.biz.service.handler.account;

import io.g740.commons.exception.impl.AuthAccountNotFoundException;
import io.g740.commons.exception.impl.ResourceDuplicateException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.account.AccountProfileDto;
import io.g740.pigeon.biz.object.dto.account.CreateAccountDto;
import io.g740.pigeon.biz.object.dto.account.UpdateAccountDto;
import io.g740.pigeon.biz.object.entity.account.CredentialDo;
import io.g740.pigeon.biz.object.entity.account.MembershipDo;
import io.g740.pigeon.biz.object.entity.account.UserDo;
import io.g740.pigeon.biz.persistence.jpa.account.CredentialRepository;
import io.g740.pigeon.biz.persistence.jpa.account.MembershipRepository;
import io.g740.pigeon.biz.persistence.jpa.account.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author bbottong
 */
@Handler
public class AccountHandler {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Transactional(rollbackFor = Exception.class)
    public AccountProfileDto createAccount(CreateAccountDto createAccountDto,
                                           UserInfo operationUserInfo) throws ServiceException {
        boolean existsDuplicate = this.userRepository.existsByUsername(createAccountDto.getUsername());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(UserDo.RESOURCE_NAME + ":" + createAccountDto.getUsername());
        }

        UserDo userDo = new UserDo();
        userDo.setUsername(createAccountDto.getUsername());
        userDo.setEnabled(createAccountDto.getEnabled());
        BaseDo.create(userDo, operationUserInfo.getUsername(), new Date());
        this.userRepository.save(userDo);

        CredentialDo credentialDo = new CredentialDo();
        credentialDo.setUsername(userDo.getUsername());
        credentialDo.setCredential(DigestUtils.sha256Hex(createAccountDto.getPassword()));
        BaseDo.create(credentialDo, operationUserInfo.getUsername(), new Date());
        this.credentialRepository.save(credentialDo);

        MembershipDo membershipDo = new MembershipDo();
        membershipDo.setUsername(userDo.getUsername());
        membershipDo.setRole(createAccountDto.getRole());
        BaseDo.create(membershipDo, operationUserInfo.getUsername(), new Date());
        this.membershipRepository.save(membershipDo);

        return getAccountProfile(userDo.getUsername(), operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public AccountProfileDto updateAccount(UpdateAccountDto updateAccountDto,
                                           UserInfo operationUserInfo) throws ServiceException {
        UserDo userDo = this.userRepository.findByUsername(updateAccountDto.getUsername());
        if (userDo == null) {
            throw new ResourceNotFoundException(UserDo.RESOURCE_NAME + ":" + updateAccountDto.getUsername());
        }
        MembershipDo membershipDo = this.membershipRepository.findByUsername(updateAccountDto.getUsername());
        if (membershipDo == null) {
            throw new ResourceNotFoundException(MembershipDo.RESOURCE_NAME + ":" + updateAccountDto.getUsername());
        }

        boolean requiredToUpdateUserDo = false;
        if (updateAccountDto.getEnabled() != null &&
                !updateAccountDto.getEnabled().equals(userDo.getEnabled())) {
            requiredToUpdateUserDo = true;
            userDo.setEnabled(updateAccountDto.getEnabled());
        }

        if (requiredToUpdateUserDo) {
            BaseDo.update(userDo, operationUserInfo.getUsername(), new Date());
            this.userRepository.save(userDo);
        }

        boolean requiredToUpdateMembershipDo = false;
        if (updateAccountDto.getRole() != null &&
                !updateAccountDto.getRole().equalsIgnoreCase(membershipDo.getRole())) {
            requiredToUpdateMembershipDo = true;
            membershipDo.setRole(updateAccountDto.getRole());
        }

        if (requiredToUpdateMembershipDo) {
            BaseDo.update(membershipDo, operationUserInfo.getUsername(), new Date());
            this.membershipRepository.save(membershipDo);
        }

        return getAccountProfile(userDo.getUsername(), operationUserInfo);
    }

    public AccountProfileDto getAccountProfile(String username, UserInfo operationUserInfo) throws ServiceException {
        UserDo userDo = this.userRepository.findByUsername(username);
        if (userDo == null) {
            throw new AuthAccountNotFoundException(username);
        }
        AccountProfileDto accountProfileDto = new AccountProfileDto();
        accountProfileDto.setUsername(username);
        accountProfileDto.setEmailAddress(userDo.getEmailAddress());
        accountProfileDto.setPhone(userDo.getPhone());
        accountProfileDto.setEnabled(userDo.getEnabled());
        accountProfileDto.setCreateTimestamp(userDo.getCreateTimestamp());

        // 获取用户membership
        MembershipDo membershipDo = this.membershipRepository.findByUsername(username);
        if (membershipDo != null) {
            accountProfileDto.setRole(membershipDo.getRole());
        }

        return accountProfileDto;
    }

    public List<String> queryUsername(String username, UserInfo operationUserInfo) throws ServiceException {
        return this.userRepository.fuzzyQueryByUsername(username);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(String username, UserInfo operationUserInfo) throws ServiceException {
        UserDo userDo = this.userRepository.findByUsername(username);
        if (userDo == null) {
            throw new AuthAccountNotFoundException(username);
        }

        userDo.setDeleted(true);

        BaseDo.update(userDo, operationUserInfo.getUsername(), new Date());
        this.userRepository.save(userDo);
    }
}
