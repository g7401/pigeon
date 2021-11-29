package io.g740.pigeon.biz.api.account;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.account.AccountProfileDto;
import io.g740.pigeon.biz.object.dto.account.CreateAccountDto;
import io.g740.pigeon.biz.object.dto.account.UpdateAccountDto;
import io.g740.pigeon.biz.service.interfaces.account.AccountService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bbottong
 */
@Api(tags = "Account APIs")
@RestController
@RequestMapping(value = "/account")
public class AccountApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AccountService accountService;

    @ApiOperation("创建Account")
    @PostMapping("")
    @ResponseBody
    public Response<AccountProfileDto> createAccount(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateAccountDto createAccountDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (Strings.isNullOrEmpty(createAccountDto.getUsername())) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(createAccountDto.getPassword())) {
            throw new IllegalParameterException("password is required");
        }
        if (Strings.isNullOrEmpty(createAccountDto.getRole())) {
            throw new IllegalParameterException("role is required");
        }
        if (!createAccountDto.getRole().equalsIgnoreCase(MembershipConstants.MEMBERSHIP_USER) &&
                !createAccountDto.getRole().equalsIgnoreCase(MembershipConstants.MEMBERSHIP_DEVELOPER) &&
                !createAccountDto.getRole().equalsIgnoreCase(MembershipConstants.MEMBERSHIP_ADMIN)) {
            throw new IllegalParameterException("role should be USER/DEVELOPER/ADMIN");
        }
        if (createAccountDto.getEnabled() == null) {
            throw new IllegalParameterException("enabled is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.accountService.createAccount(createAccountDto, operationUserInfo));
    }

    @ApiOperation("更新Account")
    @PatchMapping("")
    @ResponseBody
    public Response<AccountProfileDto> updateAccount(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateAccountDto updateAccountDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (Strings.isNullOrEmpty(updateAccountDto.getUsername())) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(updateAccountDto.getRole()) &&
                updateAccountDto.getEnabled() == null) {
            throw new IllegalParameterException("role, enabled, at least 1 is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.accountService.updateAccount(updateAccountDto, operationUserInfo));
    }

    @ApiOperation("删除Account")
    @DeleteMapping("")
    @ResponseBody
    public void deleteAccount(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "username") String username) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        this.accountService.deleteAccount(username, operationUserInfo);
    }

    @ApiOperation("获取指定用户的Account Profile")
    @PostMapping("/profile")
    @ResponseBody
    public Response<AccountProfileDto> getAccountProfile(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "username") String username) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.accountService.getAccountProfile(username, operationUserInfo));
    }

    @ApiOperation("用户名查询")
    @GetMapping("/username/query")
    @ResponseBody
    public Response<List<String>> queryUsername(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "username") String username) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.accountService.queryUsername(username, operationUserInfo));
    }
}
