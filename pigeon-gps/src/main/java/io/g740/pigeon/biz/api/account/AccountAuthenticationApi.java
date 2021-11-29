package io.g740.pigeon.biz.api.account;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.oauth.SignInDto;
import io.g740.pigeon.biz.object.dto.oauth.SignedInDto;
import io.g740.pigeon.biz.service.interfaces.account.AccountService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author bbottong
 */
@Api(tags = "Account OAuth APIs")
@RestController
@RequestMapping(value = "/account/oauth")
public class AccountAuthenticationApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AccountService accountService;

    @ApiOperation("用户登录")
    @PostMapping("/sign_in")
    @ResponseBody
    public Response<SignedInDto> signIn(
            @RequestBody SignInDto signInDto) throws Exception {
        // 参数校验
        if (Strings.isNullOrEmpty(signInDto.getUsername())) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(signInDto.getPassword())) {
            throw new IllegalParameterException("password is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.authenticationService.signIn(signInDto));
    }

    @ApiOperation("用户注销")
    @PostMapping("/sign_out")
    public void signOut(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "username") String username) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        this.authenticationService.authenticateUser(username, accessToken);

        // 业务逻辑
        this.authenticationService.signOut(username);
    }
}
