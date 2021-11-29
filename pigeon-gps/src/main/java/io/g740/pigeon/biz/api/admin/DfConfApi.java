package io.g740.pigeon.biz.api.admin;

import com.alibaba.excel.util.CollectionUtils;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.ds.IndexDto;
import io.g740.pigeon.biz.service.interfaces.admin.DfService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bbottong
 */
@Api(tags = "Data Facet（DF，数据面）配置相关APIs")
@RestController
@RequestMapping(value = "/admin/df")
public class DfConfApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DfService dfService;

    @ApiOperation("获取指定Data Facet的配置，包括基本配置和高级配置")
    @GetMapping("/conf")
    @ResponseBody
    public Response<DfConfDto> getDfConf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_uid") Long dfUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dfService.getDfConf(dfUid, operationUserInfo));
    }

    @ApiOperation("获取指定Data Facet的配置，只包括基本配置")
    @GetMapping("/conf/basic")
    @ResponseBody
    public Response<PageResult<DfConfDataFieldDto>> getDfConfBasic(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_uid") Long dfUid,
            Pageable pageable) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dfService.getDfConfBasic(dfUid, pageable, operationUserInfo));
    }

    @ApiOperation("获取指定Data Facet的配置，只包括高级配置")
    @GetMapping("/conf/advanced")
    @ResponseBody
    public Response<DfConfAdvancedDto> getDfConfAdvanced(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_uid") Long dfUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dfService.getDfConfAdvanced(dfUid, operationUserInfo));
    }

    @ApiOperation("创建或者替换指定Data Facet的配置，包括基本配置和高级配置")
    @PutMapping("/conf")
    @ResponseBody
    public Response<DfConfDto> createOrReplaceDfConf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateOrReplaceDfConfDto createOrReplaceDfConfDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createOrReplaceDfConfDto.getDfUid() == null) {
            throw new IllegalParameterException("df_uid is required");
        }
        if (createOrReplaceDfConfDto.getBasic() == null) {
            throw new IllegalParameterException("basic is required");
        }
        if (CollectionUtils.isEmpty(createOrReplaceDfConfDto.getBasic().getFields())) {
            throw new IllegalParameterException("fields is required in basic");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dfService.createOrReplaceDfConf(createOrReplaceDfConfDto, operationUserInfo));
    }

    @ApiOperation("刷新指定Data Facet的可用字段，即从Data Facet所依存的表/视图重新加载字段")
    @PostMapping("/conf/refresh_df_available_data_fields")
    @ResponseBody
    public Response<DfRefreshResultDto> refreshDfAvailableDataFields(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_uid") Long dfUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(this.dfService.refreshDfAvailableDataFields(dfUid, operationUserInfo));
    }

    @ApiOperation("将指定源Data Facet的配置复制给指定目标Data Facet")
    @PostMapping("/conf/replication")
    @ResponseBody
    public Response<DfConfDto> replicateDfConf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "source_df_uid") Long sourceDfUid,
            @RequestParam(name = "destination_df_uid") Long destinationDfUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        this.dfService.replicateDfConf(sourceDfUid, destinationDfUid, operationUserInfo);
        return Response.buildSuccess(this.dfService.getDfConf(destinationDfUid, operationUserInfo));
    }

    @ApiOperation("加载指定Data Facet所依存的表的索引")
    @PostMapping("/conf/indexes")
    @ResponseBody
    public Response<List<IndexDto>> loadIndexes(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_uid") Long dfUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(this.dfService.loadIndexes(dfUid, operationUserInfo));
    }
}
