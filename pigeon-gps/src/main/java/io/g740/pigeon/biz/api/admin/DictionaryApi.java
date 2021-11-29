package io.g740.pigeon.biz.api.admin;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.pigeon.biz.object.dto.dictionary.*;
import io.g740.pigeon.biz.service.interfaces.admin.DictionaryService;
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
@Api(tags = "Dictionary（字典）定义、配置和使用相关APIs")
@RestController
@RequestMapping(value = "/admin/dictionary")
public class DictionaryApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DictionaryService dictionaryService;

    @ApiOperation("创建字典类目")
    @PostMapping("/categories")
    @ResponseBody
    public Response<DictionaryCategoryDto> createDictionaryCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDictionaryCategoryDto createDictionaryCategoryDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (Strings.isNullOrEmpty(createDictionaryCategoryDto.getName())) {
            throw new IllegalParameterException("name is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.createDictionaryCategory(createDictionaryCategoryDto, operationUserInfo));
    }

    @ApiOperation("更新指定字典类目")
    @PatchMapping("/categories")
    @ResponseBody
    public Response<DictionaryCategoryDto> updateDictionaryCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDictionaryCategoryDto updateDictionaryCategoryDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDictionaryCategoryDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }
        if (Strings.isNullOrEmpty(updateDictionaryCategoryDto.getName()) &&
                Strings.isNullOrEmpty(updateDictionaryCategoryDto.getDescription())) {
            throw new IllegalParameterException("name, description, at least 1 is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.updateDictionaryCategory(updateDictionaryCategoryDto, operationUserInfo));
    }

    @ApiOperation("删除指定字典类目")
    @DeleteMapping("/categories")
    public void deleteDictionaryCategory(
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
        this.dictionaryService.deleteDictionaryCategory(uid, operationUserInfo);
    }

    @ApiOperation("列出所有字典类目")
    @GetMapping("/categories")
    public Response<List<DictionaryCategoryDto>> listAllDictionaryCategories(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(this.dictionaryService.listAllDictionaryCategories(operationUserInfo));
    }

    @ApiOperation("为指定字典类目创建字典结构")
    @PostMapping("/structures")
    @ResponseBody
    public Response<DictionaryStructureDto> createDictionaryStructure(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDictionaryStructureDto createDictionaryStructureDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createDictionaryStructureDto.getDictionaryCategoryUid() == null) {
            throw new IllegalParameterException("dictionary_category_uid is required");
        }
        if (Strings.isNullOrEmpty(createDictionaryStructureDto.getName())) {
            throw new IllegalParameterException("name is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.createDictionaryStructure(createDictionaryStructureDto, operationUserInfo));
    }

    @ApiOperation("更新指定字典结构")
    @PatchMapping("/structures")
    @ResponseBody
    public Response<DictionaryStructureDto> updateDictionaryStructure(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDictionaryStructureDto updateDictionaryStructureDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDictionaryStructureDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }
        if (Strings.isNullOrEmpty(updateDictionaryStructureDto.getName()) &&
                Strings.isNullOrEmpty(updateDictionaryStructureDto.getDescription())) {
            throw new IllegalParameterException("name, description, at least 1 is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.updateDictionaryStructure(updateDictionaryStructureDto, operationUserInfo));
    }

    @ApiOperation("删除指定字典结构")
    @DeleteMapping("/structures")
    public void deleteDictionaryStructure(
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
        this.dictionaryService.deleteDictionaryStructure(uid, operationUserInfo);
    }

    @ApiOperation("列出所有字典结构")
    @GetMapping("/structures/list")
    @ResponseBody
    public Response<SimpleTreeNode> listAllDictionaryStructures(
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
                this.dictionaryService.listAllDictionaryStructures(operationUserInfo));
    }

    @ApiOperation("为指定字典类目创建字典构建流程定义")
    @PostMapping("/build_process_def")
    @ResponseBody
    public Response<DictionaryBuildProcessDefDto> createDictionaryBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDictionaryBuildProcessDefDto createDictionaryBuildProcessDefDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createDictionaryBuildProcessDefDto.getDictionaryCategoryUid() == null) {
            throw new IllegalParameterException("dictionary_category_uid is required");
        }

        if (createDictionaryBuildProcessDefDto.getScheduleType() == null) {
            throw new IllegalParameterException("schedule_type is required");
        }

        if (ScheduleTypeEnum.PERIODIC.equals(createDictionaryBuildProcessDefDto.getScheduleType())) {
            if (Strings.isNullOrEmpty(createDictionaryBuildProcessDefDto.getScheduleTypeExtDetails())) {
                throw new IllegalParameterException("schedule_type_ext_details is required when schedule_type is PERIODIC");
            }
            // 测试cron表达式
            try {
                new CronTrigger(createDictionaryBuildProcessDefDto.getScheduleTypeExtDetails());
            } catch (Exception e) {
                throw new IllegalParameterException("illegal cron expression, " +
                        createDictionaryBuildProcessDefDto.getScheduleTypeExtDetails() + ". More info:" + e.getMessage(), e);
            }
        }

        if (createDictionaryBuildProcessDefDto.getBuildStrategyType() == null) {
            throw new IllegalParameterException("build_strategy_type is required");
        }
        switch (createDictionaryBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                if (createDictionaryBuildProcessDefDto.getSqlBuildStrategyContent() == null) {
                    throw new IllegalParameterException("sql_build_strategy_content is required");
                }
                break;
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.createDictionaryBuildProcessDef(createDictionaryBuildProcessDefDto,
                        operationUserInfo));
    }

    @ApiOperation("查询指定字典类目的字典构建流程定义")
    @GetMapping("/build_process_def/query_by_dictionary_category")
    @ResponseBody
    public Response<DictionaryBuildProcessDefDto> queryDictionaryBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "dictionary_category_uid") Long dictionaryCategoryUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.queryDictionaryBuildProcessDef(dictionaryCategoryUid, operationUserInfo));
    }

    @ApiOperation("获取字典构建流程定义")
    @GetMapping("/build_process_def")
    @ResponseBody
    public Response<DictionaryBuildProcessDefDto> getDictionaryBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "process_def_uid") Long processDefUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        return Response.buildSuccess(
                this.dictionaryService.getDictionaryBuildProcessDef(processDefUid, operationUserInfo));
    }

    @ApiOperation("更新指定字典构建流程定义")
    @PatchMapping("/build_process_def")
    @ResponseBody
    public Response<DictionaryBuildProcessDefDto> updateDictionaryBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDictionaryBuildProcessDefDto updateDictionaryBuildProcessDefDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDictionaryBuildProcessDefDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.updateDictionaryBuildProcessDef(updateDictionaryBuildProcessDefDto,
                        operationUserInfo));
    }

    @ApiOperation("测试字典构建流程定义")
    @PostMapping("/build_process_def/test")
    @ResponseBody
    public Response<SimpleTreeNode> testDictionaryBuildProcessDef(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody TestDictionaryBuildProcessDefDto testDictionaryBuildProcessDefDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (testDictionaryBuildProcessDefDto.getBuildStrategyType() == null) {
            throw new IllegalParameterException("build_strategy_type is required");
        }
        switch (testDictionaryBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                if (testDictionaryBuildProcessDefDto.getSqlBuildStrategyContent() == null) {
                    throw new IllegalParameterException("sql_build_strategy_content is required");
                }
                break;
        }

        // 业务逻辑
        return Response.buildSuccess(this.dictionaryService.testDictionaryBuildProcessDef(
                testDictionaryBuildProcessDefDto, operationUserInfo));
    }

    @ApiOperation("查询字典构建流程实例")
    @GetMapping("/build_process_inst/query")
    @ResponseBody
    public Response<PageResult<DictionaryBuildProcessInstDto>> queryDictionaryBuildProcessInst(
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
        return Response.buildSuccess(this.dictionaryService.queryDictionaryBuildProcessInst(processDefUid,
                parameterMap, pageable, operationUserInfo));
    }

    @ApiOperation("列出所有字典内容")
    @GetMapping("/content/list")
    @ResponseBody
    public Response<SimpleTreeNode> listAllDictionaryContent(
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
                this.dictionaryService.listAllDictionaryContent(operationUserInfo));
    }

    @ApiOperation("列出指定字典类目的字典内容")
    @GetMapping("/content")
    @ResponseBody
    public Response<SimpleTreeNode> getDictionaryContentByDictionaryCategory(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "dictionary_category_uid") Long dictionaryCategoryUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.getDictionaryContentByDictionaryCategory(dictionaryCategoryUid, operationUserInfo));
    }

    @ApiOperation("列出指定字典类目和/或指定字典内容的下一级字典内容")
    @GetMapping("/content/next_level")
    @ResponseBody
    public Response<SimpleTreeNode> getNextLevelDictionaryContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "dictionary_category_uid") Long dictionaryCategoryUid,
            @RequestParam(name = "dictionary_content_uid", required = false) Long dictionaryContentUid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.getNextLevelDictionaryContent(dictionaryCategoryUid, dictionaryContentUid, operationUserInfo));
    }

    @ApiOperation("模糊搜索指定字典类目的字典内容")
    @GetMapping("/content/query")
    @ResponseBody
    public Response<SimpleTreeNode> queryDictionaryContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "dictionary_category_uid") Long dictionaryCategoryUid,
            @RequestParam(name = "label", required = false) String label) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.queryDictionaryContent(dictionaryCategoryUid, label, operationUserInfo));
    }

    @ApiOperation("创建字典内容")
    @PostMapping("/content")
    @ResponseBody
    public Response<DictionaryContentDto> createDictionaryContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody CreateDictionaryContentDto createDictionaryContentDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (createDictionaryContentDto.getDictionaryCategoryUid() == null) {
            throw new IllegalParameterException("dictionary_category_uid is required");
        }
        if (Strings.isNullOrEmpty(createDictionaryContentDto.getValue())) {
            throw new IllegalParameterException("value is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.createDictionaryContent(createDictionaryContentDto, operationUserInfo));
    }

    @ApiOperation("更新指定字典内容")
    @PatchMapping("/content")
    @ResponseBody
    public Response<DictionaryContentDto> updateDictionaryContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestBody UpdateDictionaryContentDto updateDictionaryContentDto) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (updateDictionaryContentDto.getUid() == null) {
            throw new IllegalParameterException("uid is required");
        }
        if (Strings.isNullOrEmpty(updateDictionaryContentDto.getValue()) &&
                Strings.isNullOrEmpty(updateDictionaryContentDto.getLabel())) {
            throw new IllegalParameterException("value, label, at least 1 is required");
        }

        // 业务逻辑
        return Response.buildSuccess(
                this.dictionaryService.updateDictionaryContent(updateDictionaryContentDto, operationUserInfo));
    }

    @ApiOperation("删除指定字典内容")
    @DeleteMapping("/content")
    public void deleteDictionaryContent(
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
        this.dictionaryService.deleteDictionaryContent(uid, operationUserInfo);
    }
}
