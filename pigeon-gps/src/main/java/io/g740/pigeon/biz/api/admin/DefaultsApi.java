package io.g740.pigeon.biz.api.admin;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.defaults.*;
import io.g740.pigeon.biz.service.interfaces.admin.DefaultsService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Api(tags = "Defaults（默认值）定义、配置和使用相关APIs")
@RestController
@RequestMapping(value = "/admin/defaults")
public class DefaultsApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DefaultsService defaultsService;

    @ApiOperation("创建默认值类目")
    @PostMapping("/categories")
    @ResponseBody
    public Response<DefaultsCategoryDto> createDefaultsCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDefaultsCategoryDto createDefaultsCategoryDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (Strings.isNullOrEmpty(createDefaultsCategoryDto.getName())) {
            throw new IllegalParameterException("name is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.createDefaultsCategory(createDefaultsCategoryDto, operationUserInfo));
    }

    @ApiOperation("更新指定默认值类目")
    @PatchMapping("/categories")
    @ResponseBody
    public Response<DefaultsCategoryDto> updateDefaultsCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDefaultsCategoryDto updateDefaultsCategoryDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDefaultsCategoryDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }
        if (Strings.isNullOrEmpty(updateDefaultsCategoryDto.getName()) &&
                Strings.isNullOrEmpty(updateDefaultsCategoryDto.getDescription())) {
            throw new IllegalParameterException("name or description, at least 1 is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.updateDefaultsCategory(updateDefaultsCategoryDto, operationUserInfo));
    }

    @ApiOperation("删除指定默认值类目")
    @DeleteMapping("/categories")
    public void deleteDefaultsCategory(
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
        this.defaultsService.deleteDefaultsCategory(uid, operationUserInfo);
    }

    @ApiOperation("列出所有默认值类目")
    @GetMapping("/categories/list")
    @ResponseBody
    public Response<SimpleTreeNode> listAllDefaultsCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.listAllDefaultsCategory(operationUserInfo));
    }

    @ApiOperation("查询默认值类目")
    @GetMapping("/categories/query")
    @ResponseBody
    public Response<PageResult<DefaultsCategoryDto>> queryDefaultsCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "name", required = false) List<String> names,
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
                this.defaultsService.queryDefaultsCategory(names, pageable, operationUserInfo));
    }

    @ApiOperation("为指定默认值类目创建默认值构建流程定义")
    @PostMapping("/build_process_def")
    @ResponseBody
    public Response<DefaultsBuildProcessDefDto> createDefaultsBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDefaultsBuildProcessDefDto createDefaultsBuildProcessDefDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createDefaultsBuildProcessDefDto.getScheduleType() == null) {
            throw new IllegalParameterException("schedule_type is required");
        }

        if (ScheduleTypeEnum.PERIODIC.equals(createDefaultsBuildProcessDefDto.getScheduleType())) {
            if (Strings.isNullOrEmpty(createDefaultsBuildProcessDefDto.getScheduleTypeExtDetails())) {
                throw new IllegalParameterException("schedule_type_ext_details is required when schedule_type is PERIODIC");
            }
            // 测试cron表达式

            try {
                new CronTrigger(createDefaultsBuildProcessDefDto.getScheduleTypeExtDetails());
            } catch (Exception e) {
                throw new IllegalParameterException("illegal cron expression, " +
                        createDefaultsBuildProcessDefDto.getScheduleTypeExtDetails() + ". More info:" + e.getMessage(), e);
            }
        }

        if (createDefaultsBuildProcessDefDto.getBuildStrategyType() == null) {
            throw new IllegalParameterException("build_strategy_type is required");
        }
        switch (createDefaultsBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                if (createDefaultsBuildProcessDefDto.getSqlBuildStrategyContent() == null) {
                    throw new IllegalParameterException("sql_build_strategy_content is required");
                }
                break;
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.createDefaultsBuildProcessDef(createDefaultsBuildProcessDefDto,
                        operationUserInfo));
    }

    @ApiOperation("查询指定默认值类目的默认值构建流程定义")
    @GetMapping("/build_process_def/query_by_defaults_category")
    @ResponseBody
    public Response<DefaultsBuildProcessDefDto> queryDefaultsBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "defaults_category_uid") Long defaultsCategoryUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.queryDefaultsBuildProcessDef(defaultsCategoryUid, operationUserInfo));
    }

    @ApiOperation("获取默认值构建流程定义")
    @GetMapping("/build_process_def")
    @ResponseBody
    public Response<DefaultsBuildProcessDefDto> getDefaultsBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "process_def_uid") Long processDefUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.getDefaultsBuildProcessDef(processDefUid, operationUserInfo));
    }

    @ApiOperation("更新默认值构建定义")
    @PatchMapping("/build_process_def")
    @ResponseBody
    public Response<DefaultsBuildProcessDefDto> updateDefaultsBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDefaultsBuildProcessDefDto updateDefaultsBuildProcessDefDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDefaultsBuildProcessDefDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.updateDefaultsBuildProcessDef(updateDefaultsBuildProcessDefDto,
                        operationUserInfo));
    }

    @ApiOperation("测试默认值构建流程定义")
    @PostMapping("/build_process_def/test")
    @ResponseBody
    public Response<SimpleTreeNode> testDefaultsBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody TestDefaultsBuildProcessDefDto testDefaultsBuildProcessDefDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (testDefaultsBuildProcessDefDto.getBuildStrategyType() == null) {
            throw new IllegalParameterException("build_strategy_type is required");
        }
        switch (testDefaultsBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                if (testDefaultsBuildProcessDefDto.getSqlBuildStrategyContent() == null) {
                    throw new IllegalParameterException("sql_build_strategy_content is required");
                }
                break;
        }

        // 业务逻辑
        return Response.buildSuccess(this.defaultsService.testDefaultsBuildProcessDef(
                testDefaultsBuildProcessDefDto, operationUserInfo));
    }

    @ApiOperation("查询默认值构建流程实例")
    @GetMapping("/build_process_inst/query")
    @ResponseBody
    public Response<PageResult<DefaultsBuildProcessInstDto>> queryDefaultsBuildProcessInst(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "process_def_uid") Long processDefUid,
            Pageable pageable,
            HttpServletRequest request) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        parameterMap.remove("process_def_uid");

        // 业务逻辑
        return Response.buildSuccess(this.defaultsService.queryDefaultsBuildProcessInst(processDefUid,
                parameterMap, pageable, operationUserInfo));
    }

    @ApiOperation("列出所有默认值内容")
    @GetMapping("/content/list")
    @ResponseBody
    public Response<SimpleTreeNode> listAllDefaultsContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.listAllDefaultsContent(operationUserInfo));
    }

    @ApiOperation("列出指定默认值类目的内容")
    @GetMapping("/content")
    @ResponseBody
    public Response<SimpleTreeNode> getDefaultsContentByDefaultsCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "defaults_category_uid") Long defaultsCategoryUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.getDefaultsContentByDefaultsCategory(defaultsCategoryUid, operationUserInfo));
    }


    @ApiOperation("创建默认值内容")
    @PostMapping("/content")
    @ResponseBody
    public Response<DefaultsContentDto> createDefaultsContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDefaultsContentDto createDefaultsContentDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createDefaultsContentDto.getDefaultsCategoryUid() == null) {
            throw new IllegalParameterException("defaults_category_uid is required");
        }
        if (Strings.isNullOrEmpty(createDefaultsContentDto.getValue())) {
            throw new IllegalParameterException("value is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.createDefaultsContent(createDefaultsContentDto, operationUserInfo));
    }

    @ApiOperation("更新指定默认值内容")
    @PatchMapping("/content")
    @ResponseBody
    public Response<DefaultsContentDto> updateDefaultsContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDefaultsContentDto updateDefaultsContentDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDefaultsContentDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }
        if (Strings.isNullOrEmpty(updateDefaultsContentDto.getValue()) &&
                Strings.isNullOrEmpty(updateDefaultsContentDto.getLabel())) {
            throw new IllegalParameterException("value, label, at least 1 is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.defaultsService.updateDefaultsContent(updateDefaultsContentDto, operationUserInfo));
    }

    @ApiOperation("删除指定默认值内容")
    @DeleteMapping("/content")
    public void deleteDefaultsContent(
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
        this.defaultsService.deleteDefaultsContent(uid, operationUserInfo);
    }
}
