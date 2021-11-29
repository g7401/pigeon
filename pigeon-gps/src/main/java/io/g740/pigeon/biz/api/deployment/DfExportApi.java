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
import io.g740.pigeon.biz.object.dto.export.AsyncExportJobDto;
import io.g740.pigeon.biz.service.interfaces.deployment.DfDeploymentService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.share.constants.AsyncExportFileTypeEnum;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author bbottong
 */
@Api(tags = "Data Facet（DF，数据面）导出相关APIs")
@RestController
@RequestMapping(value = "/deployment/df")
public class DfExportApi {
    @Autowired
    private OperationService operationService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DfDeploymentService dfDeploymentService;

    @ApiOperation("异步导出指定Data Facet的内容至CSV文件（采用UTF-8 with BOM字符编码）")
    @PostMapping("/async_export_jobs/csv")
    @ResponseBody
    public Response<AsyncExportJobDto> asyncExportDfContentByCsv(
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
        AsyncExportJobDto response = this.dfDeploymentService.asyncExportDfContent(AsyncExportFileTypeEnum.CSV, dfKey, parameterMap,
                dfAdvancedQueryDto, pageable.getSort(), operationUserInfo);

        // Audit logging
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                OperationDo.COMMON_TASK_API_OUTGOING,
                null,
                JSONObject.toJSONString(response));

        return Response.buildSuccess(response);
    }

    @ApiOperation("异步导出指定Data Facet的内容至CSV压缩文件（采用UTF-8 with BOM字符编码，ZIP压缩）")
    @PostMapping("/async_export_jobs/csv_and_compress")
    @ResponseBody
    public Response<AsyncExportJobDto> asyncExportDfContentByCsvAndCompress(
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
        AsyncExportJobDto response = this.dfDeploymentService.asyncExportDfContent(
                AsyncExportFileTypeEnum.CSV_AND_COMPRESS, dfKey, parameterMap,
                dfAdvancedQueryDto, pageable.getSort(), operationUserInfo);

        // Audit logging
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                OperationDo.COMMON_TASK_API_OUTGOING,
                null,
                JSONObject.toJSONString(response));

        return Response.buildSuccess(response);
    }

    @ApiOperation("异步导出指定Data Facet的内容至Excel文件，最多导出1,048,576行（含表头行）")
    @PostMapping("/async_export_jobs/excel")
    @ResponseBody
    public Response<AsyncExportJobDto> asyncExportDfContentByExcel(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "df_key") String dfKey,
            @RequestBody DfAdvancedQueryDto dfAdvancedQueryDto,
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
        AsyncExportJobDto response = this.dfDeploymentService.asyncExportDfContent(
                AsyncExportFileTypeEnum.EXCEL, dfKey, parameterMap,
                dfAdvancedQueryDto, pageable.getSort(), operationUserInfo);

        // Audit logging
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                OperationDo.COMMON_TASK_API_OUTGOING,
                null,
                JSONObject.toJSONString(response));

        return Response.buildSuccess(response);
    }

    @ApiOperation("获取指定异步导出工作")
    @GetMapping("/async_export_jobs")
    @ResponseBody
    public Response<AsyncExportJobDto> getAsyncExportJob(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        return Response.buildSuccess(
                this.dfDeploymentService.getAsyncExportJob(uid, operationUserInfo));
    }

    @ApiOperation("查询异步导出工作")
    @GetMapping("/async_export_jobs/query")
    @ResponseBody
    public Response<PageResult<AsyncExportJobDto>> queryAsyncExportJob(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
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
                this.dfDeploymentService.queryAsyncExportJob(pageable, operationUserInfo));
    }

    @ApiOperation("删除指定异步导出工作，限定只能删除当前登录用户创建的且状态为CANCELED或FAILED的异步导出工作")
    @DeleteMapping("/async_export_jobs")
    public void deleteAsyncExportJob(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        this.dfDeploymentService.deleteAsyncExportJob(uid, operationUserInfo);
    }

    @ApiOperation("取消指定异步导出工作，限定只能取消当前登录用户创建的且状态为WAITING的异步导出工作")
    @PatchMapping("/async_export_jobs/cancel")
    public void cancelAsyncExportJob(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // 鉴权和授权
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        UserInfo operationUserInfo = this.authenticationService.validateAccessTokenAndRetrieveUserInfo(accessToken);
        this.authorizationService.authorizeUserRole(operationUserInfo,
                MembershipConstants.MEMBERSHIP_USER,
                MembershipConstants.MEMBERSHIP_DEVELOPER,
                MembershipConstants.MEMBERSHIP_ADMIN);

        // 业务逻辑
        this.dfDeploymentService.cancelAsyncExportJob(uid, operationUserInfo);
    }

}
