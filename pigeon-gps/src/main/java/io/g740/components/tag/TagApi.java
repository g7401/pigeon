package io.g740.components.tag;

import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
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
@Api(tags = "Tag APIs")
@RestController
@RequestMapping(value = "/general")
public class TagApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private TagHandler tagHandler;

    @Autowired
    private AccountService accountService;

    @ApiOperation("列出所有tags")
    @GetMapping("/tags/list")
    @ResponseBody
    public Response<List<String>> listAllTags(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);

        // 业务逻辑
        return Response.buildSuccess(this.tagHandler.listAllTags(operationUserInfo));
    }

    @ApiOperation("创建tag")
    @PostMapping("/tags")
    @ResponseBody
    public Response<String> createTag(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "tag") String tag) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(this.tagHandler.createTag(tag, operationUserInfo));
    }

    @ApiOperation("删除tag")
    @DeleteMapping("/tags")
    @ResponseBody
    public void deleteTag(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "tag") String tag) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        this.tagHandler.deleteTag(tag, operationUserInfo);
    }
}
