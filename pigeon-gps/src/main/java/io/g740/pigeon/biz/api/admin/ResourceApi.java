package io.g740.pigeon.biz.api.admin;

import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.service.interfaces.admin.ResourceService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author bbottong
 */
@Api(tags = "Resource（资源）定义相关APIs")
@RestController
@RequestMapping(value = "/admin/resource")
public class ResourceApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private ResourceService resourceService;

    @ApiOperation("列出所有资源类目(resource categories)及每个资源类目(resource category)的资源结构Hierarchy(resource structure hierarchy)")
    @GetMapping("/structures/list")
    @ResponseBody
    public Response<SimpleTreeNode> listAllResourceStructures(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(
                this.resourceService.listAllResourceStructures(operationUserInfo));
    }
}
