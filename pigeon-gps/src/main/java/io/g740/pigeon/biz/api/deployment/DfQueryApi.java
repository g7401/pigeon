package io.g740.pigeon.biz.api.deployment;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import io.g740.commons.api.RequestIdLogFilter;
import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.ApiUtils;
import io.g740.components.audit.OperationDo;
import io.g740.components.audit.OperationService;
import io.g740.pigeon.biz.object.dto.df.DfAdvancedQueryDto;
import io.g740.pigeon.biz.object.dto.df.DfConfForPageGenerationDto;
import io.g740.pigeon.biz.object.dto.df.DfConfTableFieldDto;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.service.interfaces.account.AccountService;
import io.g740.pigeon.biz.service.interfaces.deployment.DfDeploymentService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * @author bbottong
 */
@Api(tags = "Data Facet（DF，数据面）查询相关APIs")
@RestController
@RequestMapping(value = "/deployment/df")
public class DfQueryApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(DfQueryApi.class);

    @Autowired
    private OperationService operationService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DfDeploymentService dfDeploymentService;

    @Autowired
    private AccountService accountService;

    @ApiOperation("查询Data Facet")
    @GetMapping("/query")
    @ResponseBody
    public Response<PageResult<DfSimpleDto>> queryDfForPageGeneration(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "tag", required = false) List<String> tags,
            @RequestParam(name = "name", required = false) String name,
            Pageable pageable) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);


            // 业务逻辑
            return Response.buildSuccess(
                    this.dfDeploymentService.queryDfForPageGeneration(tags, name, pageable, operationUserInfo));
    }

    @ApiOperation("获取为指定Data Facet生成页面的配置")
    @GetMapping("/conf")
    @ResponseBody
    public Response<DfConfForPageGenerationDto> getDfConfForPageGeneration(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_key") String dfKey) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);
        this.authorizationService.authorizeUserAccessToDf(operationUserInfo, dfKey);

            // 业务逻辑
            DfConfForPageGenerationDto result = this.dfDeploymentService.getDfConfForPageGeneration(dfKey, operationUserInfo);

            return Response.buildSuccess(result);
    }

    @ApiOperation("获取为指定Data Facet生成页面的配置中的表格配置")
    @GetMapping("/conf/table")
    @ResponseBody
    public Response<List<DfConfTableFieldDto>> getDfConfTableFieldsForPageGeneration(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_key") String dfKey) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(
                this.dfDeploymentService.getDfConfTableFieldsForPageGeneration(dfKey, operationUserInfo));
    }

    @ApiOperation("获取为指定Data Facet生成页面的配置中的表格配置中的允许group by且其中的维度字段")
    @GetMapping("/conf/table/group_by_fields")
    @ResponseBody
    public Response<List<DfConfTableFieldDto>> getDfConfTableFieldsEnabledGroupByForPageGeneration(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_key") String dfKey) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(
                this.dfDeploymentService.getDfConfTableFieldsEnabledGroupByForPageGeneration(dfKey, operationUserInfo));
    }

    @ApiOperation("查询指定Data Facet的内容")
    @PostMapping("/content/query")
    @ResponseBody
    public Response<PageResult<JSONObject>> queryDfContent(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_key") String dfKey,
            @RequestBody(required = false) DfAdvancedQueryDto dfAdvancedQueryDto,
            Pageable pageable,
            HttpServletRequest request) throws Exception {
        // Audit logging
        this.operationService.createOperation(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                MDC.get(RequestIdLogFilter.REQUEST_URI_KEY),
                MDC.get(RequestIdLogFilter.REQUEST_PARAMETERS_KEY));

        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                OperationDo.COMMON_TASK_API_INCOMING,
                ApiUtils.encodeToString(request, dfAdvancedQueryDto),
                null);

        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        if (dfAdvancedQueryDto != null) {
            // group_by 和 required_return_field_names 是两个不能同时使用的功能
            if (dfAdvancedQueryDto.getGroupBy() != null && dfAdvancedQueryDto.getRequiredReturnFieldNames() != null) {
                throw new IllegalParameterException("group_by and required_return_field_names can only being request " +
                        "one at the same time");
            } else if (dfAdvancedQueryDto.getGroupBy() != null) {
                // 只用group_by功能
                if (dfAdvancedQueryDto.getGroupBy().getFieldProcessingType() == null) {
                    throw new IllegalParameterException("field_processing_type is required in group_by");
                }
                if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getGroupBy().getSelectedGroupByFieldNames())) {
                    throw new IllegalParameterException("selected_group_by_field_names is required in group by");
                }
                switch (dfAdvancedQueryDto.getGroupBy().getFieldProcessingType()) {
                    case RETURN_ONLY_SELECTED_FIELDS:
                    case RETURN_ALL_FIELDS:
                        // DO NOTHING
                        break;
                    case ADVANCED:
                        if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getGroupBy().getResultFields())) {
                            throw new IllegalParameterException("result_fields is required when field_processing_type" +
                                    " equals to ADVANCED");
                        }
                        dfAdvancedQueryDto.getGroupBy().getResultFields().forEach(dfQueryResultFieldDto -> {
                            if (dfQueryResultFieldDto == null ||
                                    Strings.isNullOrEmpty(dfQueryResultFieldDto.getFieldName())) {
                                throw new IllegalParameterException("field_name is required in " +
                                        "result_fields");
                            }
                            if (dfQueryResultFieldDto.getAggregateFunctionType() == null &&
                                    !Strings.isNullOrEmpty(dfQueryResultFieldDto.getNewFieldLabel())) {
                                throw new IllegalParameterException("new_field_label is NOT required when " +
                                        "aggregate_function_type is NULL");
                            }
                            if (dfQueryResultFieldDto.getAggregateFunctionType() != null &&
                                    Strings.isNullOrEmpty(dfQueryResultFieldDto.getNewFieldLabel())) {
                                throw new IllegalParameterException("new_field_label is required when " +
                                        "aggregate_function_type is NOT NULL");
                            }
                        });
                        break;
                    default:
                        break;
                }
            } else if (dfAdvancedQueryDto.getRequiredReturnFieldNames() != null) {
                // 只用required_return_field_names功能
                if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
                    throw new IllegalParameterException("required_return_field_names is required");
                }
            } else {
                // group_by 和 required_return_field_names 都不使用
            }
        }

        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        String dfKeyLabel = "df_key";
        parameterMap.remove(dfKeyLabel);


        // 业务逻辑
        PageResult<JSONObject> response = this.dfDeploymentService.queryDfContent(dfKey,
                parameterMap, dfAdvancedQueryDto, pageable, operationUserInfo);

        // Audit logging
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                OperationDo.COMMON_TASK_API_OUTGOING,
                null,
                JSONObject.toJSONString(response));

        return Response.buildSuccess(response);
    }

    @ApiOperation("查询指定Data Facet的内容For Graph")
    @GetMapping("/content/query/graph")
    @ResponseBody
    public Response<List<JSONObject>> queryDfContentForGraph(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_key") String dfKey,
            @RequestParam(name = "dimension_field_name") List<String> dimensionFieldNames,
            @RequestParam(name = "kpi_field_name") String kpiFieldName,
            Pageable pageable,
            HttpServletRequest request) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 参数校验
        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        String[] toRemoveParameterNames = new String[]{"df_key", "dimension_field_name", "kpi_field_name"};
        for (String parameterName : toRemoveParameterNames) {
            parameterMap.remove(parameterName);
        }

        // 业务逻辑
        return Response.buildSuccess(this.dfDeploymentService.queryDfContentForGraph(
                dfKey, dimensionFieldNames, kpiFieldName, parameterMap, pageable.getSort(), operationUserInfo));
    }
}
