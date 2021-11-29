package io.g740.pigeon.biz.api.admin;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.ds.*;
import io.g740.pigeon.biz.service.interfaces.admin.DsService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bbottong
 */
@Api(tags = "Data Source（DS, 数据源）定义、配置和使用相关APIs")
@RestController
@RequestMapping(value = "/admin/ds")
public class DsApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DsService dsService;

    @ApiOperation("创建数据源")
    @PostMapping("")
    @ResponseBody
    public Response<DsDto> createDs(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDsDto createDsDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (Strings.isNullOrEmpty(createDsDto.getName())) {
            throw new IllegalParameterException("name is required");
        }
        if (createDsDto.getType() == null) {
            throw new IllegalParameterException("type is required");
        }
        if (Strings.isNullOrEmpty(createDsDto.getConnectionProfileProps())) {
            throw new IllegalParameterException("connection_profile_props is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dsService.createDs(createDsDto, operationUserInfo));
    }

    @ApiOperation("更新指定数据源")
    @PatchMapping("")
    @ResponseBody
    public Response<DsDto> updateDs(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDsDto updateDsDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDsDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dsService.updateDs(updateDsDto, operationUserInfo));
    }

    @ApiOperation("删除指定数据源")
    @DeleteMapping("")
    public void deleteDs(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        this.dsService.deleteDs(uid, operationUserInfo);
    }

    @ApiOperation("获取指定数据源")
    @GetMapping("")
    @ResponseBody
    public Response<DsDto> getDs(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.dsService.getDs(uid, operationUserInfo));
    }

    @ApiOperation("列出所有数据源")
    @GetMapping("/list")
    @ResponseBody
    public Response<List<DsDto>> listAllDs(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.dsService.listAllDs(operationUserInfo));
    }

    @ApiOperation("测试数据源连接")
    @PostMapping("/test_connection")
    @ResponseBody
    public Response<Boolean> testDsConnection(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody TestDsConnectionDto testDsConnectionDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (testDsConnectionDto.getDsType() == null) {
            throw new IllegalParameterException("ds_type is required");
        }
        if (Strings.isNullOrEmpty(testDsConnectionDto.getConnectionProfileProps())) {
            throw new IllegalParameterException("connection_profile_props is required");
        }

        // 业务逻辑
        return Response.buildSuccess(this.dsService.testDsConnection(testDsConnectionDto, operationUserInfo));
    }

    @ApiOperation("测试在数据源上执行查询语句")
    @PostMapping("/test_query_statement")
    @ResponseBody
    public Response<SimpleQueryResult> testQueryStatement(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody TestQueryStatementDto testQueryStatementDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (testQueryStatementDto.getDsUid() == null) {
            throw new IllegalParameterException("ds_uid is required");
        }
        if (Strings.isNullOrEmpty(testQueryStatementDto.getQueryStatement())) {
            throw new IllegalParameterException("query_statement is required");
        }

        // 业务逻辑
        return Response.buildSuccess(this.dsService.testQueryStatement(testQueryStatementDto, operationUserInfo));
    }

    @ApiOperation("刷新指定数据源")
    @PostMapping("/refresh_one")
    @ResponseBody
    public Response<DsRefreshResultDto> refreshDs(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "ds_uid") Long dsUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.dsService.refreshDs(dsUid, operationUserInfo));
    }

    @ApiOperation("刷新所有数据源")
    @PostMapping("/refresh_all")
    @ResponseBody
    public Response<List<DsRefreshResultDto>> refreshAllDs(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.dsService.refreshAllDs(operationUserInfo));
    }
}
