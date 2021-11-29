package io.g740.pigeon.biz.api.admin;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.app.*;
import io.g740.pigeon.biz.service.interfaces.admin.AppService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bbottong
 */
@Api(tags = "App定义和配置相关APIs")
@RestController
@RequestMapping(value = "/admin/app")
public class AppApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AppService appService;

    @ApiOperation("创建App")
    @PostMapping("")
    @ResponseBody
    public Response<AppDto> createApp(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateAppDto createAppDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createAppDto.getEnabled() == null) {
            throw new IllegalParameterException("enabled is required");
        }
        if (Strings.isNullOrEmpty(createAppDto.getAppName())) {
            throw new IllegalParameterException("app_name is required");
        }
        if (Strings.isNullOrEmpty(createAppDto.getAppKey())) {
            throw new IllegalParameterException("app_key is required");
        }
        if (Strings.isNullOrEmpty(createAppDto.getAppSecret())) {
            throw new IllegalParameterException("app_secret is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.appService.createApp(createAppDto, operationUserInfo));
    }

    @ApiOperation("更新指定App")
    @PatchMapping("")
    @ResponseBody
    public Response<AppDto> updateApp(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateAppDto updateAppDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateAppDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }
        if (Strings.isNullOrEmpty(updateAppDto.getAppName()) &&
                Strings.isNullOrEmpty(updateAppDto.getAppSecret()) &&
                updateAppDto.getEnabled() == null) {
            throw new IllegalParameterException("app_name, app_secret and enabled, at least 1 is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.appService.updateApp(updateAppDto, operationUserInfo));
    }

    @ApiOperation("删除指定App")
    @DeleteMapping("")
    public void deleteApp(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        this.appService.deleteApp(uid, operationUserInfo);
    }

    @ApiOperation("获取指定App")
    @GetMapping("")
    @ResponseBody
    public Response<AppDto> getApp(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.appService.getApp(uid, operationUserInfo));
    }

    @ApiOperation("查询App")
    @GetMapping("/query")
    @ResponseBody
    public Response<PageResult<AppDto>> queryApp(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "app_name", required = false) String appName,
            Pageable pageable) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.appService.queryApp(appName, pageable, operationUserInfo));
    }

    @ApiOperation("获取指定App能访问哪些Data Facets的配置")
    @GetMapping("/access/df")
    @ResponseBody
    public Response<List<AppDfDto>> getAppAccessToDf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "app_uid") Long appUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.appService.getAppAccessToDf(appUid, operationUserInfo));
    }

    @ApiOperation("替换指定App能访问哪些Data Facets的配置")
    @PutMapping("/access/df")
    @ResponseBody
    public void replaceAppAccessToDf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody ReplaceAppAccessToDfDto replaceAppAccessToDfDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (replaceAppAccessToDfDto.getAppUid() == null) {
            throw new IllegalParameterException("app_uid is required");
        }
        if (CollectionUtils.isEmpty(replaceAppAccessToDfDto.getDfUids())) {
            throw new IllegalParameterException("df_uids is required");
        }

        // 业务逻辑
        this.appService.replaceAppAccessToDf(replaceAppAccessToDfDto, operationUserInfo);
    }

    @ApiOperation("获取指定App能被哪些用户访问的配置")
    @GetMapping("/access/user")
    @ResponseBody
    public Response<List<String>> getAppAccessByUser(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "app_uid") Long appUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.appService.getAppAccessByUser(appUid, operationUserInfo));
    }

    @ApiOperation("替换指定App能被哪些用户访问的配置")
    @PutMapping("/access/user")
    @ResponseBody
    public void replaceAppAccessByUser(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody ReplaceAppAccessByUserDto replaceAppAccessByUserDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (replaceAppAccessByUserDto.getAppUid() == null) {
            throw new IllegalParameterException("app_uid is required");
        }
        if (CollectionUtils.isEmpty(replaceAppAccessByUserDto.getUsernames())) {
            throw new IllegalParameterException("usernames is required");
        }

        // 业务逻辑
        this.appService.replaceAppAccessByUser(replaceAppAccessByUserDto, operationUserInfo);
    }
}
