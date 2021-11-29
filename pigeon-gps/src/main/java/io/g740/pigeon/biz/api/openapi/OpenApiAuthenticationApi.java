package io.g740.pigeon.biz.api.openapi;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.oauth.AccessTokenDto;
import io.g740.pigeon.biz.object.dto.oauth.CreateAccessTokenDto;
import io.g740.pigeon.biz.object.dto.oauth.RefreshAccessTokenDto;
import io.g740.pigeon.biz.object.dto.oauth.RefreshedAccessTokenDto;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author bbottong
 */
@Api(tags = "OpenApi OAuth APIs")
@RestController
@RequestMapping(value = "/openapi/oauth")
public class OpenApiAuthenticationApi {
    @Autowired
    private AuthenticationService authenticationService;

    @ApiOperation("创建Access Token")
    @PostMapping("/access_token")
    @ResponseBody
    public Response<AccessTokenDto> createAccessToken(
            @RequestBody CreateAccessTokenDto createAccessTokenDto) throws Exception {
        if (Strings.isNullOrEmpty(createAccessTokenDto.getClientId())) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(createAccessTokenDto.getGrantType())) {
            throw new IllegalParameterException("grant_type is required");
        }
        if (!ApiUtils.OAUTH_GRANT_TYPE_CLIENT_CREDENTIALS.equalsIgnoreCase(createAccessTokenDto.getGrantType())) {
            throw new IllegalParameterException("grant_type should be " + ApiUtils.OAUTH_GRANT_TYPE_CLIENT_CREDENTIALS);
        }
        if (Strings.isNullOrEmpty(createAccessTokenDto.getSignature())) {
            throw new IllegalParameterException("signature is required");
        }

        return Response.buildSuccess(
                this.authenticationService.createAccessToken(createAccessTokenDto));
    }

    @ApiOperation("刷新Access Token")
    @PutMapping("/access_token")
    @ResponseBody
    public Response<RefreshedAccessTokenDto> refreshAccessToken(
            @RequestBody RefreshAccessTokenDto refreshAccessTokenDto) throws Exception {
        if (Strings.isNullOrEmpty(refreshAccessTokenDto.getClientId())) {
            throw new IllegalParameterException("client_id shout is required");
        }
        if (Strings.isNullOrEmpty(refreshAccessTokenDto.getRefreshToken())) {
            throw new IllegalParameterException("refresh_token required");
        }
        if (Strings.isNullOrEmpty(refreshAccessTokenDto.getGrantType())) {
            throw new IllegalParameterException("grant_type required");
        }
        if (!ApiUtils.OAUTH_GRANT_TYPE_REFRESH_TOKEN.equalsIgnoreCase(refreshAccessTokenDto.getGrantType())) {
            throw new IllegalParameterException("grant_type should be " + ApiUtils.OAUTH_GRANT_TYPE_REFRESH_TOKEN);
        }
        if (Strings.isNullOrEmpty(refreshAccessTokenDto.getSignature())) {
            throw new IllegalParameterException("signature is required");
        }

        return Response.buildSuccess(
                this.authenticationService.refreshAccessToken(refreshAccessTokenDto));
    }
}
