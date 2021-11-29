package io.g740.pigeon.base.api;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.base.object.dto.CreateSystemReleaseDto;
import io.g740.pigeon.base.object.dto.SystemReleaseDto;
import io.g740.pigeon.base.service.interfaces.SystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author bbottong
 */
@Api(tags = "System定义和配置相关APIs")
@RestController
@RequestMapping(value = "/system")
public class SystemApi {
    @Autowired
    private SystemService systemService;

    @ApiOperation("创建最新系统发布信息")
    @PostMapping("/parameters/latest_system_release")
    @ResponseBody
    public Response<SystemReleaseDto> createLatestSystemRelease(
            @RequestHeader(NetworkingConstants.HEADER_USERNAME) String username,
            @RequestBody CreateSystemReleaseDto createSystemReleaseDto) throws Exception {
        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);

        if (Strings.isNullOrEmpty(createSystemReleaseDto.getReleaseVersion())) {
            throw new IllegalParameterException("release_version is required");
        }

        return Response.buildSuccess(
                this.systemService.createSystemRelease(createSystemReleaseDto, operationUserInfo));
    }

    @ApiOperation("获取最新系统发布信息")
    @GetMapping("/parameters/latest_system_release")
    @ResponseBody
    public Response<SystemReleaseDto> getLatestSystemRelease() throws Exception {
        return Response.buildSuccess(
                this.systemService.getLatestSystemRelease());
    }
}
