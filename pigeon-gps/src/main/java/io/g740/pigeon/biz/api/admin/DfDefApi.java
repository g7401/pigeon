package io.g740.pigeon.biz.api.admin;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.df.CreateOrReplaceDfDto;
import io.g740.pigeon.biz.object.dto.df.DfDto;
import io.g740.pigeon.biz.service.interfaces.admin.DfService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author bbottong
 */
@Api(tags = "Data Facet（DF，数据面）定义相关APIs")
@RestController
@RequestMapping(value = "/admin/df")
public class DfDefApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DfService dfService;

    @ApiOperation("创建Data Facet或替换指定Data Facet")
    @PutMapping("/def")
    @ResponseBody
    public Response<DfDto> createOrReplaceDf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateOrReplaceDfDto createOrReplaceDfDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createOrReplaceDfDto.getKey() == null) {
            throw new IllegalParameterException("key is required");
        }
        if (createOrReplaceDfDto.getName() == null) {
            throw new IllegalParameterException("name is required");
        }
        if (createOrReplaceDfDto.getPrimaryDataObjectUid() == null) {
            throw new IllegalParameterException("primary_data_object_uid is required");
        }
        if (createOrReplaceDfDto.getProcessingNeeded() != null &&
                Boolean.TRUE.equals(createOrReplaceDfDto.getProcessingNeeded())) {
            if (Strings.isNullOrEmpty(createOrReplaceDfDto.getProcessingLogic())) {
                throw new IllegalParameterException("processing_logic is required if processing_needed is true");
            }
        } else {
            if (!Strings.isNullOrEmpty(createOrReplaceDfDto.getProcessingLogic())) {
                throw new IllegalParameterException("processing_logic is not required if processing_needed is false " +
                        "or null");
            }
        }
        if (CollectionUtils.isNotEmpty(createOrReplaceDfDto.getSecondaryDataObjectUids())) {
            for (Long secondaryDataObjectUid : createOrReplaceDfDto.getSecondaryDataObjectUids()) {
                if (secondaryDataObjectUid == null) {
                    throw new IllegalParameterException("secondary_data_object_uids cannot contain null item");
                }
                if (createOrReplaceDfDto.getPrimaryDataObjectUid().equals(secondaryDataObjectUid)) {
                   throw new IllegalParameterException("secondary_data_object_uids cannot contain primary_data_object_uid");
                }
            }
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dfService.createOrReplaceDf(createOrReplaceDfDto, operationUserInfo));
    }

    @ApiOperation("删除指定Data Facet")
    @DeleteMapping("/def")
    public void deleteDf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        this.dfService.deleteDf(uid, operationUserInfo);
    }

    @ApiOperation("获取指定Data Facet")
    @GetMapping("/def")
    @ResponseBody
    public Response<DfDto> getDf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dfService.getDfByUid(uid, operationUserInfo));
    }

    @ApiOperation("列出所有Data Objects，如果任意Data Object建立了Data Facet(s)，也同时列出这些Data Facet(s)")
    @GetMapping("/def/list")
    @ResponseBody
    public Response<SimpleTreeNode> listAllDataObjectsWithOrWithoutDf(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "show_df_only") Boolean showDfOnly) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dfService.listAllDataObjectsWithOrWithoutDf(showDfOnly, operationUserInfo));
    }
}
