package io.g740.pigeon.biz.api.openapi;

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
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.export.AsyncExportJobDto;
import io.g740.pigeon.biz.rpc.DataPermissionsRpcHandler;
import io.g740.pigeon.biz.service.interfaces.admin.DictionaryService;
import io.g740.pigeon.biz.service.interfaces.deployment.DfDeploymentService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthorizationService;
import io.g740.pigeon.biz.service.interfaces.openapi.OpenApiService;
import io.g740.pigeon.biz.share.constants.AsyncExportFileTypeEnum;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Api(tags = "OpenApi APIs")
@RestController
@RequestMapping(value = "/openapi")
public class OpenApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApi.class);

    @Autowired
    private OperationService operationService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private OpenApiService openApiService;

    @Autowired
    private DfDeploymentService dfDeploymentService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DataPermissionsRpcHandler cuckooRpcHandler;

    @ApiOperation("??????????????????App?????????Data Facet")
    @GetMapping("/df/paged_list")
    @ResponseBody
    public Response<PageResult<DfSimpleDto>> pagedListAllDfForApp(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "client_id") String clientId,
            Pageable pageable) throws Exception {
        // ???????????????
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????
        return Response.buildSuccess(
                this.openApiService.pagedListAllDfForApp(clientId, pageable));
    }

    @ApiOperation("????????????App?????????Data Facet")
    @GetMapping("/df/list")
    @ResponseBody
    public Response<List<DfSimpleDto>> listAllDfForApp(
            @RequestHeader(NetworkingConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestParam(name = "client_id") String clientId) throws Exception {
        // ???????????????
        String accessToken = ApiUtils.extractAccessTokenFromAuthorizationHeader(ApiUtils.BEARER_TOKEN_TYPE, authorization);
        this.authenticationService.authenticateApp(clientId, accessToken);

        return Response.buildSuccess(
                this.openApiService.listAllDfForApp(clientId));
    }

    @ApiOperation("???????????????App?????????Data Facet???????????????????????????")
    @GetMapping("/df/conf")
    @ResponseBody
    public Response<DfConfForPageGenerationDto> getDfConfForPageGeneration(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "df_key") String dfKey
    ) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        return Response.buildSuccess(
                this.dfDeploymentService.getDfConfForPageGeneration(dfKey, operationUserInfo));
    }

    @ApiOperation("???????????????App?????????Data Facet?????????????????????????????????????????????")
    @GetMapping("/df/conf/table")
    @ResponseBody
    public Response<List<DfConfTableFieldDto>> getDfConfTableFieldsForPageGeneration(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "df_key") String dfKey
    ) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        return Response.buildSuccess(
                this.dfDeploymentService.getDfConfTableFieldsForPageGeneration(dfKey, operationUserInfo));
    }

    @ApiOperation("???????????????App?????????Data Facet?????????????????????????????????????????????????????????group by??????")
    @GetMapping("/df/conf/table/group_by_fields")
    @ResponseBody
    public Response<List<DfConfTableFieldDto>> getDfConfTableFieldsEnabledGroupByForPageGeneration(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "df_key") String dfKey
    ) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        return Response.buildSuccess(
                this.dfDeploymentService.getDfConfTableFieldsEnabledGroupByForPageGeneration(dfKey, operationUserInfo));
    }

    @ApiOperation("????????????App?????????Data Facet?????????")
    @PostMapping("/df/content/query")
    @ResponseBody
    public Response<PageResult<JSONObject>> queryDfContent(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
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

        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        if (dfAdvancedQueryDto != null) {
            // group_by ??? required_return_field_names ????????????????????????????????????
            if (dfAdvancedQueryDto.getGroupBy() != null && dfAdvancedQueryDto.getRequiredReturnFieldNames() != null) {
                throw new IllegalParameterException("group_by and required_return_field_names can only being request " +
                        "one at the same time");
            } else if (dfAdvancedQueryDto.getGroupBy() != null) {
                // ??????group_by??????
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
                // ??????required_return_field_names??????
                if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
                    throw new IllegalParameterException("required_return_field_names is required");
                }
            } else {
                // group_by ??? required_return_field_names ????????????
            }
        }

        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.ACCESS_TOKEN_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.CLIENT_ID_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.USERNAME_PARAMETER_NAME);
        String dfKeyLabel = "df_key";
        parameterMap.remove(dfKeyLabel);

        // ??????resource limit parameters
        parameterMap = buildResourceLimitParameters(dfKey, username, parameterMap, pageable, operationUserInfo);
        if (parameterMap == null) {
            PageResult pageResult = PageResult.buildEmpty(pageable.getPageNumber(), pageable.getPageSize());
            return Response.buildSuccess(pageResult);
        }

        // transform in order to support swagger
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

    /**
     * ??????resource limit parameters for df
     *
     * @param dfKey
     * @param username
     * @param parameterMap
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws Exception
     */
    private Map<String, String[]> buildResourceLimitParameters(
            String dfKey, String username, Map<String, String[]> parameterMap,
            Pageable pageable, UserInfo operationUserInfo) throws Exception {
        /**
         * ?????????DF??????????????????????????????????????????????????????resource category??????
         */
        List<DfConfTableFieldDto> listOfDfConfTableFieldDto =
                this.dfDeploymentService.getDfConfTableFieldsWithResourceCategoryForPageGeneration(dfKey, operationUserInfo);
        if (CollectionUtils.isNotEmpty(listOfDfConfTableFieldDto)) {
            // by resource category uid ??????
            List<Long> listOfResourceCategoryUid = new ArrayList<>();
            Map<Long, List<DfConfTableFieldDto>> mapOfResourceCategory = new HashMap<>();
            for (DfConfTableFieldDto dfConfTableFieldDto : listOfDfConfTableFieldDto) {
                List<DfConfTableFieldDto> collectionsOfResourceCategory =
                        mapOfResourceCategory.get(dfConfTableFieldDto.getResourceCategoryUid());
                if (collectionsOfResourceCategory == null) {
                    collectionsOfResourceCategory = new ArrayList<>();
                    listOfResourceCategoryUid.add(dfConfTableFieldDto.getResourceCategoryUid());
                }
                collectionsOfResourceCategory.add(dfConfTableFieldDto);
                mapOfResourceCategory.put(dfConfTableFieldDto.getResourceCategoryUid(), collectionsOfResourceCategory);
            }

            // ???Cuckoo??????????????????????????????????????????resource category?????????resource content
            SimpleTreeNode rootTreeNode;
            try {
                // Audit logging
                Map<String, Object> inputMap = new HashMap<>();
                inputMap.put("username", username);
                inputMap.put("listOfResourceCategoryUid", listOfResourceCategoryUid);
                this.operationService.createOperationTask(
                        MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                        "this.cuckooRpcHandler.getResourceContent",
                        JSONObject.toJSONString(inputMap),
                        null);

                rootTreeNode = this.cuckooRpcHandler.getResourceContent(
                        username, listOfResourceCategoryUid, operationUserInfo);

                // Audit logging
                this.operationService.createOperationTask(
                        MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                        "this.cuckooRpcHandler.getResourceContent",
                        null,
                        JSONObject.toJSONString(rootTreeNode));
            } catch (Exception e) {
                // Audit logging
                this.operationService.createOperationTask(
                        MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                        "this.cuckooRpcHandler.getResourceContent",
                        null,
                        e.getMessage());
                // ??????rpc???????????????????????????????????????resource category?????????????????????resource content???
                // ??????????????????????????????
                return null;
            }

            if (rootTreeNode == null) {
                // ??????????????????resource category??????????????????resource content???
                // ?????????????????????
                return null;
            } else {
                List<SimpleTreeNode> resourceCategoryTreeNodes = rootTreeNode.getChildren();
                if (CollectionUtils.isEmpty(resourceCategoryTreeNodes)) {
                    // ??????????????????resource category??????????????????resource content???
                    // ?????????????????????
                    return null;
                } else {
                    for (SimpleTreeNode resourceCategoryTreeNode : resourceCategoryTreeNodes) {
                        Map<String, String[]> resourceLimitParameterMap =
                                buildResourceLimitParameters(resourceCategoryTreeNode,
                                        mapOfResourceCategory.get(resourceCategoryTreeNode.getUidCode()));
                        // ????????????????????????????????????resource limit?????????
                        for (Map.Entry<String, String[]> parameter : parameterMap.entrySet()) {
                            if (resourceLimitParameterMap.containsKey(parameter.getKey())) {
                                // ?????????????????????????????????resource limit parameter??????????????????????????????????????????
                                List<String> allowedListOfParameterValue = new ArrayList<>();
                                for (String parameterValue : parameter.getValue()) {
                                    boolean found = false;
                                    for (String resourceLimit : resourceLimitParameterMap.get(parameter.getKey())) {
                                        if (parameterValue.equalsIgnoreCase(resourceLimit)) {
                                            found = true;
                                        }
                                    }
                                    if (found) {
                                        allowedListOfParameterValue.add(parameterValue);
                                    }
                                }
                                String[] allowedArrayOfParameterValue = new String[allowedListOfParameterValue.size()];
                                allowedListOfParameterValue.toArray(allowedArrayOfParameterValue);
                                parameterMap.put(parameter.getKey(), allowedArrayOfParameterValue);
                            }
                        }

                        // ????????????resource limit?????????????????????????????????????????????????????????
                        List<String> missedParameterName = new ArrayList<>();
                        for (Map.Entry<String, String[]> parameter : resourceLimitParameterMap.entrySet()) {
                            if (!parameterMap.containsKey(parameter.getKey())) {
                                missedParameterName.add(parameter.getKey());
                            }
                        }
                        // ???????????????????????????????????????
                        if (CollectionUtils.isNotEmpty(missedParameterName)) {
                            for (String parameterName : missedParameterName) {
                                parameterMap.put(parameterName, resourceLimitParameterMap.get(parameterName));
                            }
                        }
                    }
                }
            }
        }

        // Audit logging
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                "buildResourceLimitParameters",
                null,
                JSONObject.toJSONString(parameterMap));

        return parameterMap;
    }

    /**
     * ??????resource limit parameters for ??????resource category for ????????????
     *
     * @param resourceCategoryTreeNode
     * @param listOfDfConfTableFieldDto
     * @return
     */
    private Map<String, String[]> buildResourceLimitParameters(
            SimpleTreeNode resourceCategoryTreeNode, List<DfConfTableFieldDto> listOfDfConfTableFieldDto) {
        Map<String, String[]> parameterMap = new HashMap<>();

        for (DfConfTableFieldDto dfConfTableFieldDto : listOfDfConfTableFieldDto) {
            String fieldName = dfConfTableFieldDto.getFieldName();
            String resourceStructureName = dfConfTableFieldDto.getResourceStructureName();
            List<String> parameterValues = new ArrayList<>();
            buildResourceLimitParameters(resourceCategoryTreeNode, resourceStructureName, parameterValues);
            if (CollectionUtils.isNotEmpty(parameterValues)) {
                String[] arrayOfParameterValue = new String[parameterValues.size()];
                parameterValues.toArray(arrayOfParameterValue);
                parameterMap.put(fieldName, arrayOfParameterValue);
            }
        }

        return parameterMap;
    }

    /**
     * ??????resource limit parameters for ????????????
     *
     * @param treeNode
     * @param resourceStructureName
     * @param parameterValues
     */
    private void buildResourceLimitParameters(
            SimpleTreeNode treeNode, String resourceStructureName, List<String> parameterValues) {
        if (treeNode.getType().equalsIgnoreCase(resourceStructureName)) {
            parameterValues.add(treeNode.getName());
        } else {
            if (CollectionUtils.isNotEmpty(treeNode.getChildren())) {
                for (SimpleTreeNode childTreeNode : treeNode.getChildren()) {
                    buildResourceLimitParameters(childTreeNode, resourceStructureName, parameterValues);
                }
            }
        }
    }

    @ApiOperation("????????????App?????????Data Facet?????????For Graph")
    @GetMapping("/df/content/query/graph")
    @ResponseBody
    public Response<List<JSONObject>> queryDfContentForGraph(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "df_key") String dfKey,
            @RequestParam(name = "dimension_field_name") List<String> dimensionFieldNames,
            @RequestParam(name = "kpi_field_name") String kpiFieldName,
            Pageable pageable,
            HttpServletRequest request) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.CLIENT_ID_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.ACCESS_TOKEN_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.USERNAME_PARAMETER_NAME);
        String[] toRemoveParameterNames = new String[]{"df_key", "dimension_field_name", "kpi_field_name"};
        for (String parameterName : toRemoveParameterNames) {
            parameterMap.remove(parameterName);
        }

        // ??????resource limit parameters
        parameterMap = buildResourceLimitParameters(dfKey, username, parameterMap, pageable, operationUserInfo);
        if (parameterMap == null) {
            return Response.buildSuccess(null);
        }

        // transform in order to support swagger
        return Response.buildSuccess(this.dfDeploymentService.queryDfContentForGraph(
                dfKey, dimensionFieldNames, kpiFieldName, parameterMap, pageable.getSort(), operationUserInfo));
    }

    @ApiOperation("??????????????????Data Facet????????????CSV???????????????UTF-8 with BOM???????????????")
    @PostMapping("/df/async_export_jobs/csv")
    @ResponseBody
    public Response<AsyncExportJobDto> asyncExportDfContentByCsv(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "df_key") String dfKey,
            @RequestParam(name = "username") String username,
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


        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        if (dfAdvancedQueryDto != null) {
            // group_by ??? required_return_field_names ????????????????????????????????????
            if (dfAdvancedQueryDto.getGroupBy() != null && dfAdvancedQueryDto.getRequiredReturnFieldNames() != null) {
                throw new IllegalParameterException("group_by and required_return_field_names can only being request " +
                        "one at the same time");
            } else if (dfAdvancedQueryDto.getGroupBy() != null) {
                // ??????group_by??????
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
                // ??????required_return_field_names??????
                if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
                    throw new IllegalParameterException("required_return_field_names is required");
                }
            } else {
                // group_by ??? required_return_field_names ????????????
            }
        }

        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.ACCESS_TOKEN_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.CLIENT_ID_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.USERNAME_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        String dfKeyLabel = "df_key";
        parameterMap.remove(dfKeyLabel);

        // ??????resource limit parameters
        parameterMap = buildResourceLimitParameters(dfKey, username, parameterMap, pageable, operationUserInfo);
        if (parameterMap == null) {
            return Response.buildSuccess(null);
        }

        AsyncExportJobDto response =
                this.dfDeploymentService.asyncExportDfContent(AsyncExportFileTypeEnum.CSV, dfKey, parameterMap,
                        dfAdvancedQueryDto, pageable.getSort(), operationUserInfo);

        // Audit logging
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                OperationDo.COMMON_TASK_API_OUTGOING,
                null,
                JSONObject.toJSONString(response));

        return Response.buildSuccess(response);
    }

    @ApiOperation("??????????????????Data Facet????????????CSV?????????????????????UTF-8 with BOM???????????????ZIP?????????")
    @PostMapping("/df/async_export_jobs/csv_and_compress")
    @ResponseBody
    public Response<AsyncExportJobDto> asyncExportDfContentByCsvAndCompress(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "df_key") String dfKey,
            @RequestParam(name = "username") String username,
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


        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        if (dfAdvancedQueryDto != null) {
            // group_by ??? required_return_field_names ????????????????????????????????????
            if (dfAdvancedQueryDto.getGroupBy() != null && dfAdvancedQueryDto.getRequiredReturnFieldNames() != null) {
                throw new IllegalParameterException("group_by and required_return_field_names can only being request " +
                        "one at the same time");
            } else if (dfAdvancedQueryDto.getGroupBy() != null) {
                // ??????group_by??????
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
                // ??????required_return_field_names??????
                if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
                    throw new IllegalParameterException("required_return_field_names is required");
                }
            } else {
                // group_by ??? required_return_field_names ????????????
            }
        }

        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.ACCESS_TOKEN_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.CLIENT_ID_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.USERNAME_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        parameterMap.remove("df_key");

        // ??????resource limit parameters
        parameterMap = buildResourceLimitParameters(dfKey, username, parameterMap, pageable, operationUserInfo);
        if (parameterMap == null) {
            return Response.buildSuccess(null);
        }

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

    @ApiOperation("??????????????????Data Facet????????????Excel?????????????????????1,048,576?????????????????????")
    @PostMapping("/df/async_export_jobs/excel")
    @ResponseBody
    public Response<AsyncExportJobDto> asyncExportDfContentByExcel(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "df_key") String dfKey,
            @RequestParam(name = "username") String username,
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


        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);
        this.authorizationService.authorizeAppAccessToDf(clientId, dfKey);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }
        if (Strings.isNullOrEmpty(dfKey)) {
            throw new IllegalParameterException("df_key is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        if (dfAdvancedQueryDto != null) {
            if (dfAdvancedQueryDto.getGroupBy() != null && dfAdvancedQueryDto.getRequiredReturnFieldNames() != null) {
                throw new IllegalParameterException("group_by and required_return_field_names can only being request " +
                        "one at the same time");
            }
            if (dfAdvancedQueryDto.getGroupBy() != null) {
                if (dfAdvancedQueryDto.getGroupBy().getFieldProcessingType() == null) {
                    throw new IllegalParameterException("field_processing_type is required in group_by");
                }
                if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getGroupBy().getSelectedGroupByFieldNames())) {
                    throw new IllegalParameterException("selected_group_by_field_names is required in group by");
                }
                switch (dfAdvancedQueryDto.getGroupBy().getFieldProcessingType()) {
                    case ADVANCED:
                        if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getGroupBy().getResultFields())) {
                            throw new IllegalParameterException("result_fields is required when field_processing_type" +
                                    " equals to ADVANCED");
                        }
                        dfAdvancedQueryDto.getGroupBy().getResultFields().forEach(dfQueryResultFieldDto -> {
                            if (dfQueryResultFieldDto.getAggregateFunctionType() != null &&
                                    Strings.isNullOrEmpty(dfQueryResultFieldDto.getNewFieldLabel())) {
                                throw new IllegalParameterException("new_field_label is required when " +
                                        "aggregate_function_type is NOT NULL");
                            }
                        });
                        break;
                }
            }
            if (dfAdvancedQueryDto.getRequiredReturnFieldNames() != null) {
                if (CollectionUtils.isEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
                    throw new IllegalParameterException("required_return_field_names is required");
                }
            }
        }

        Map<String, String[]> parameterMap = ApiUtils.copy(request.getParameterMap());
        parameterMap.remove(ApiUtils.ACCESS_TOKEN_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.CLIENT_ID_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.USERNAME_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.PAGE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SIZE_PARAMETER_NAME);
        parameterMap.remove(ApiUtils.SORT_PARAMETER_NAME);
        parameterMap.remove("df_key");

        // ??????resource limit parameters
        parameterMap = buildResourceLimitParameters(dfKey, username, parameterMap, pageable, operationUserInfo);
        if (parameterMap == null) {
            return Response.buildSuccess(null);
        }

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

    @ApiOperation("????????????????????????")
    @GetMapping("/df/async_export_jobs/query")
    @ResponseBody
    public Response<PageResult<AsyncExportJobDto>> queryAsyncExportJob(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            Pageable pageable) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        return Response.buildSuccess(
                this.dfDeploymentService.queryAsyncExportJob(pageable, operationUserInfo));
    }

    @ApiOperation("??????????????????????????????")
    @GetMapping("/df/async_export_jobs")
    @ResponseBody
    public Response<AsyncExportJobDto> getAsyncExportJob(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        return Response.buildSuccess(
                this.dfDeploymentService.getAsyncExportJob(uid, operationUserInfo));
    }

    @ApiOperation("??????????????????????????????????????????????????????????????????????????????????????????CANCELED???FAILED?????????????????????")
    @DeleteMapping("/df/async_export_jobs")
    public void deleteAsyncExportJob(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        this.dfDeploymentService.deleteAsyncExportJob(uid, operationUserInfo);
    }

    @ApiOperation("??????????????????????????????????????????????????????????????????????????????????????????WAITING??????RUNNING?????????????????????")
    @PatchMapping("/df/async_export_jobs/cancel")
    public void cancelAsyncExportJob(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "uid") Long uid) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalParameterException("client_id is required");
        }
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalParameterException("access_token is required");
        }
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalParameterException("username is required");
        }

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        this.dfDeploymentService.cancelAsyncExportJob(uid, operationUserInfo);
    }

    @ApiOperation("???????????????????????????/?????????????????????????????????????????????")
    @GetMapping("/dictionary/content/next_level")
    @ResponseBody
    public Response<SimpleTreeNode> getNextLevelDictionaryContent(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "dictionary_category_uid") Long dictionaryCategoryUid,
            @RequestParam(name = "dictionary_content_uid", required = false) Long dictionaryContentUid) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        // ????????????
        return Response.buildSuccess(
                this.dictionaryService.getNextLevelDictionaryContent(dictionaryCategoryUid, dictionaryContentUid, operationUserInfo));
    }

    @ApiOperation("???????????????????????????????????????????????????????????????????????????????????????")
    @GetMapping("/dictionary/content/with_selected")
    @ResponseBody
    public Response<SimpleTreeNode> listDictionaryContentByDictionaryCategoryWithSelectedDictionaryContent(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "dictionary_category_uid") Long dictionaryCategoryUid,
            @RequestParam(name = "selected_dictionary_content_uid", required = false) List<Long> listOfSelectedDictionaryContentUid) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        // ????????????
        return Response.buildSuccess(
                this.dictionaryService.listDictionaryContentByDictionaryCategoryWithSelectedDictionaryContent(
                        dictionaryCategoryUid, listOfSelectedDictionaryContentUid, operationUserInfo));
    }

    @ApiOperation("?????????????????????????????????????????????")
    @GetMapping("/dictionary/content/query")
    @ResponseBody
    public Response<SimpleTreeNode> queryDictionaryContent(
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "access_token") String accessToken,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "dictionary_category_uid") Long dictionaryCategoryUid,
            @RequestParam(name = "label", required = false) String label) throws Exception {
        // ???????????????
        this.authenticationService.authenticateApp(clientId, accessToken);

        // ????????????

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);
        operationUserInfo.setRoleName(MembershipConstants.MEMBERSHIP_USER);

        // ????????????
        return Response.buildSuccess(
                this.dictionaryService.queryDictionaryContent(dictionaryCategoryUid, label, operationUserInfo));
    }
}
