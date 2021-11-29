package io.g740.pigeon.biz.service.handler.general;

import com.alibaba.fastjson.JSONObject;
import io.g740.commons.api.RequestIdLogFilter;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.components.audit.OperationService;
import io.g740.pigeon.biz.object.dto.df.CascadeQueryParameterDto;
import io.g740.pigeon.biz.object.dto.df.DfAdvancedQueryDto;
import io.g740.pigeon.biz.object.entity.df.DfConfDataFieldDo;
import io.g740.pigeon.biz.object.entity.df.DfDataObjectDo;
import io.g740.pigeon.biz.object.entity.df.DfDo;
import io.g740.pigeon.biz.object.entity.ds.DsDataObjectDo;
import io.g740.pigeon.biz.object.entity.ds.DsDo;
import io.g740.pigeon.biz.persistence.jpa.df.DfConfDataFieldRepository;
import io.g740.pigeon.biz.persistence.jpa.df.DfDataObjectRepository;
import io.g740.pigeon.biz.persistence.jpa.df.DfRepository;
import io.g740.pigeon.biz.persistence.jpa.ds.DsDataObjectRepository;
import io.g740.pigeon.biz.persistence.jpa.ds.DsRepository;
import io.g740.pigeon.biz.service.handler.admin.DictionaryHandler;
import io.g740.pigeon.biz.service.handler.ds.SqlTranslationHandler;
import io.g740.pigeon.biz.service.handler.ds.DsConnectionHandler;
import io.g740.pigeon.biz.share.constants.DataObjectDependencyTypeEnum;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * @author bbottong
 */
@Handler
public class DfContentQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DfContentQueryHandler.class);

    @Autowired
    private OperationService operationService;

    @Autowired
    private DfRepository dfRepository;

    @Autowired
    private DfDataObjectRepository dfDataObjectRepository;

    @Autowired
    private DsDataObjectRepository dsDataObjectRepository;

    @Autowired
    private DsRepository dsRepository;

    @Autowired
    private DfConfDataFieldRepository dfConfDataFieldRepository;

    @Autowired
    private SqlTranslationHandler sqlTranslationHandler;

    @Autowired
    private DsConnectionHandler dsConnectionHandler;

    @Autowired
    private DictionaryHandler dictionaryHandler;

    /**
     * 图形中最大点数，超过可能导致图形加载困难
     */
    @Value("${application.df.graph.maximum-dots}")
    private String graphMaximumDots;

    /**
     * 高级查询，返回Page of Row in JSONObject Format
     *
     * @param dfKey
     * @param parameterMap
     * @param dfAdvancedQueryDto
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public PageResult<JSONObject> queryDfContentAndReturnJSONObjectAsRow(
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 构建分页查询语句
        QueryStatement queryStatement =
                buildQueryStatementWithPagination(dfKey, parameterMap, dfAdvancedQueryDto, pageable, operationUserInfo);

        // Audit logging
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("dfKey", dfKey);
        inputMap.put("parameterMap", parameterMap);
        inputMap.put("dfAdvancedQueryDto", dfAdvancedQueryDto);
        inputMap.put("pageable", pageable);
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                "buildQueryStatementWithPagination",
                JSONObject.toJSONString(inputMap),
                queryStatement == null ? null : JSONObject.toJSONString(queryStatement));

        if (queryStatement == null) {
            PageResult<JSONObject> result = new PageResult<>();
            result.setTotalPages(0);
            result.setTotalElements(0L);
            result.setPageSize(pageable.getPageSize());
            result.setPageNumber(pageable.getPageNumber());
            result.setNumberOfElements(0);
            return result;
        }

        // Step 2, 执行SQL语句
        try {
            // 执行查询
            PageResult<Map<String, Object>> pageResult;

            if (pageable.getPageNumber() == 0) {
                // page number = 0, 同时执行count and query
                pageResult = this.dsConnectionHandler.executePageQuery(
                        queryStatement.getDsType(),
                        queryStatement.getDsConnectionProfileProps(),
                        queryStatement.getCountSqlStatement(),
                        queryStatement.getQuerySqlStatement(),
                        pageable);
            } else {
                // page number > 0, 只执行query
                pageResult = this.dsConnectionHandler.executePageQuery(
                        queryStatement.getDsType(),
                        queryStatement.getDsConnectionProfileProps(),
                        queryStatement.getQuerySqlStatement(),
                        pageable);
            }

            PageResult<JSONObject> result = new PageResult<>();
            result.setTotalPages(pageResult.getTotalPages());
            result.setTotalElements(pageResult.getTotalElements());
            result.setPageSize(pageResult.getPageSize());
            result.setPageNumber(pageResult.getPageNumber());
            result.setNumberOfElements(pageResult.getNumberOfElements());
            if (pageResult.getNumberOfElements() > 0) {
                result.setContent(new ArrayList<>(pageResult.getNumberOfElements()));
                pageResult.getContent().forEach(row -> {
                    JSONObject jsonObject = new JSONObject();
                    for (Map.Entry<String, Object> column : row.entrySet()) {
                        jsonObject.put(column.getKey(), column.getValue());
                    }
                    result.getContent().add(jsonObject);
                });
            }

            // Audit logging
            this.operationService.createOperationTask(
                    MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                    "executePageQuery",
                    null,
                    JSONObject.toJSONString(result));

            return result;
        } catch (Exception e) {
            String errorMessage = "failed to execute query: " + e.getMessage();
            throw new ServiceException(errorMessage, e);
        }
    }

    /**
     * Group By 查询, 返回List of Row in JSONObject Format
     *
     * @param dfKey
     * @param dimensionFieldNames Group By 维度字段列表
     * @param kpiFieldName        Group By KPI字段
     * @param parameterMap        基础查询参数
     * @param sort                排序参数
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public List<JSONObject> groupByQueryDfContentAndReturnJSONObjectAsRow(
            String dfKey,
            List<String> dimensionFieldNames,
            String kpiFieldName,
            Map<String, String[]> parameterMap,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 构建Group By查询语句
        QueryStatement queryStatement =
                buildGroupByQueryStatement(
                        dfKey, dimensionFieldNames, kpiFieldName, parameterMap,
                        sort, operationUserInfo);
        if (queryStatement == null) {
            return null;
        }

        // Step 2, 执行SQL语句
        try {
            // 执行COUNT
            SimpleQueryResult countResult = this.dsConnectionHandler.executeQuery(
                    queryStatement.getDsType(),
                    queryStatement.getDsConnectionProfileProps(),
                    queryStatement.getCountSqlStatement());

            if (countResult == null ||
                    CollectionUtils.isEmpty(countResult.getColumnNames()) ||
                    CollectionUtils.isEmpty(countResult.getRows())) {
                return null;
            }

            Long count = null;
            Map<String, Object> row0 = countResult.getRows().get(0);
            String column0 = countResult.getColumnNames().get(0);
            Object column0Value = row0.get(column0);
            if (column0Value instanceof Short) {
                count = ((Short) column0Value).longValue();
            } else if (column0Value instanceof Integer) {
                count = ((Integer) column0Value).longValue();
            } else if (column0Value instanceof Long) {
                count = (Long) column0Value;
            }

            if (count == null || count == 0) {
                return null;
            }

            Long graphMaximumDotsAsInteger = Long.parseLong(this.graphMaximumDots);
            if (count > graphMaximumDotsAsInteger) {
                throw new ServiceException("found " + count + " rows in result set, too much to load in the graph");
            }

            // 执行查询
            SimpleQueryResult queryResult = this.dsConnectionHandler.executeQuery(
                    queryStatement.getDsType(),
                    queryStatement.getDsConnectionProfileProps(),
                    queryStatement.getQuerySqlStatement());
            List<JSONObject> result = new LinkedList<>();
            queryResult.getRows().forEach(row -> {
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    jsonObject.put(column.getKey(), column.getValue());
                }
                result.add(jsonObject);
            });

            return result;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /**
     * 高级查询，返回Page of Row in Map Format
     *
     * @param dfKey
     * @param parameterMap
     * @param dfAdvancedQueryDto
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public PageResult<Map<String, Object>> queryDfContentAndReturnMapAsRow(
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 构建分页查询语句
        QueryStatement queryStatement =
                buildQueryStatementWithPagination(dfKey, parameterMap, dfAdvancedQueryDto, pageable, operationUserInfo);

        // Step 2, 执行SQL语句
        try {
            // 执行查询
            // page number = 0, 同时执行count and query
            if (pageable.getPageNumber() == 0) {
                return this.dsConnectionHandler.executePageQuery(
                        queryStatement.getDsType(),
                        queryStatement.getDsConnectionProfileProps(),
                        queryStatement.getCountSqlStatement(),
                        queryStatement.getQuerySqlStatement(),
                        pageable);
            } else {
                // page number > 0, 只执行query
                return this.dsConnectionHandler.executePageQuery(
                        queryStatement.getDsType(),
                        queryStatement.getDsConnectionProfileProps(),
                        queryStatement.getQuerySqlStatement(),
                        pageable);
            }

        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /**
     * 构造分页查询语句，含count语句和query语句，其中query语句含分页从句
     *
     * @param dfKey
     * @param parameterMap
     * @param dfAdvancedQueryDto
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public QueryStatement buildQueryStatementWithPagination(
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 找到df的定义
        // df定义
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }
        List<DfDataObjectDo> listOfDfDataObjectDo =
                this.dfDataObjectRepository.findByDfUidAndDataObjectDependencyType(
                        dfDo.getUid(), DataObjectDependencyTypeEnum.PRIMARY);
        if (CollectionUtils.isEmpty(listOfDfDataObjectDo)) {
            throw new ResourceNotFoundException(DfDataObjectDo.RESOURCE_NAME + ":" +
                    dfDo.getUid() + "," + DataObjectDependencyTypeEnum.PRIMARY);
        }
        DfDataObjectDo primaryDfDataObjectDo = listOfDfDataObjectDo.get(0);
        Long dataObjectUid = primaryDfDataObjectDo.getDataObjectUid();
        DsDataObjectDo dsDataObjectDo = this.dsDataObjectRepository.findByUid(dataObjectUid);
        if (dsDataObjectDo == null) {
            throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + dataObjectUid);
        }
        DsDo dsDo = this.dsRepository.findByUid(dsDataObjectDo.getDsUid());
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + dsDataObjectDo.getDsUid());
        }

        // Step 2, 取dictionary category and structures
        SimpleTreeNode allDictionaryStructures = this.dictionaryHandler.listAllDictionaryStructures(operationUserInfo);
        List<SimpleTreeNode> allDictionaryCategories = null;
        Map<Long, SimpleTreeNode> mapOfDictionaryCategoryTreeNode = new HashMap<>();
        if (allDictionaryStructures != null &&
                CollectionUtils.isNotEmpty(allDictionaryStructures.getChildren())) {
            allDictionaryCategories = allDictionaryStructures.getChildren();
            if (CollectionUtils.isNotEmpty(allDictionaryCategories)) {
                allDictionaryCategories.forEach(dictionaryCategoryTreeNode -> {
                    mapOfDictionaryCategoryTreeNode.put(dictionaryCategoryTreeNode.getUidCode(), dictionaryCategoryTreeNode);
                });
            }
        }

        // Step 3, 取df的form配置
        List<DfConfDataFieldDo> formListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findFormDataFieldsByDfUidAndOrderByFormElementSequenceAsc(dfDo.getUid());
        // key - field name, value - form data field
        Map<String, DfConfDataFieldDo> mapOfForm = new HashMap<>();
        // key - dictionary category id, value - first one of cascade fields
        Map<String, DfConfDataFieldDo> mapOfCascadeFields = new HashMap<>();
        if (CollectionUtils.isNotEmpty(formListOfDfConfDataFieldDoInOrder)) {
            formListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfForm.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);

                // 处理级联字段组合的第1个字段
                switch (dfConfDataFieldDo.getFormElementType()) {
                    case CASCADER:
                    case CASCADER_MULTIPLE:
                        Long dictionaryCategoryUid = dfConfDataFieldDo.getDictionaryCategoryUid();
                        if (dictionaryCategoryUid != null) {
                            if (mapOfDictionaryCategoryTreeNode.containsKey(dictionaryCategoryUid)) {
                                String dictionaryCategoryName = mapOfDictionaryCategoryTreeNode.get(dictionaryCategoryUid).getName();
                                if (Strings.isNotEmpty(dictionaryCategoryName)) {
                                    mapOfCascadeFields.put(dictionaryCategoryName, dfConfDataFieldDo);
                                }
                            }
                        }
                        break;
                }
            });
        } else {
            // 没有需要作为查询条件的字段
            // 允许继续查询，只是不需要查询条件
        }

        // Step 4, 取df的table配置
        List<DfConfDataFieldDo> tableListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(dfDo.getUid());
        Map<String, DfConfDataFieldDo> mapOfTable = new HashMap<>();
        if (CollectionUtils.isNotEmpty(tableListOfDfConfDataFieldDoInOrder)) {
            tableListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfTable.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
            });
        } else {
            // 没有需要list/table的字段
            // 查询就没有意义了，返回null
            LOGGER.warn("cannot find data fields enabled as list for df {}", dfDo.getUid());
            return null;
        }

        // Step 5, 取df的sort配置
        List<DfConfDataFieldDo> sortListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findSortDataFieldsByDfUidAndOrderBySortElementSequenceAsc(dfDo.getUid());
        Map<String, DfConfDataFieldDo> mapOfSort = new HashMap<>();
        if (CollectionUtils.isNotEmpty(sortListOfDfConfDataFieldDoInOrder)) {
            sortListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfSort.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
            });
        }

        // Step 6, 处理级联字段组合
        List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto = new ArrayList<>();
        Map<String, String[]> generalParameterMap = new HashMap<>(parameterMap.size());
        if (parameterMap != null && !parameterMap.isEmpty()) {
            parameterMap.keySet().forEach(parameterName -> {
                // 级联字段组合的参数名采用的是dictionary category name，不存在于form data fields中，需要特殊处理
                if (mapOfCascadeFields.containsKey(parameterName)) {
                    DfConfDataFieldDo dfConfDataFieldDo = mapOfCascadeFields.get(parameterName);

                    String[] arrayOfParameterValue = parameterMap.get(parameterName);

                    CascadeQueryParameterDto cascadeQueryParameterDto = new CascadeQueryParameterDto();
                    listOfCascadeQueryParameterDto.add(cascadeQueryParameterDto);
                    List<Map<String, String>> cascadeParameterMap = new ArrayList<>();
                    cascadeQueryParameterDto.setCascadeCollectionName(parameterName);
                    cascadeQueryParameterDto.setCollectionValues(cascadeParameterMap);

                    for (int i = 0; i < arrayOfParameterValue.length; i++) {
                        DfConfDataFieldDo specificDfConfDataFieldDo = dfConfDataFieldDo;
                        String parameterValue = arrayOfParameterValue[i];
                        String[] arrayOfSplitOfParameterValue = parameterValue.split(">");
                        cascadeParameterMap.add(new HashMap<>(arrayOfSplitOfParameterValue.length));
                        for (int j = 0; j < arrayOfSplitOfParameterValue.length; j++) {
                            if (specificDfConfDataFieldDo != null) {
                                cascadeParameterMap.get(i).put(specificDfConfDataFieldDo.getFieldName(), arrayOfSplitOfParameterValue[j]);
                                if (Strings.isNotEmpty(specificDfConfDataFieldDo.getChildFieldName())) {
                                    specificDfConfDataFieldDo = mapOfForm.get(specificDfConfDataFieldDo.getChildFieldName());
                                }
                            } else {
                                LOGGER.warn("null df conf data field, i=" + i + ", j=" + j);
                            }
                        }
                    }
                } else {
                    generalParameterMap.put(parameterName, parameterMap.get(parameterName));
                }
            });
        }

        // Step 7, 校验基础请求参数
        if (generalParameterMap != null && !generalParameterMap.isEmpty()) {
            generalParameterMap.keySet().forEach(parameterName -> {
                if (parameterMap.get(parameterName).length > 2000) {
                    throw new IllegalParameterException("found " + parameterMap.get(parameterName).length +
                            " parameter values for " + parameterName);
                }
            });
        }

        // Step 8, 将请求参数翻译成SQL语句
        String countSqlStatement = null;
        String querySqlStatement = null;
        if (dfAdvancedQueryDto == null) {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        tableListOfDfConfDataFieldDoInOrder,
                        mapOfSort,
                        sortListOfDfConfDataFieldDoInOrder,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        pageable,
                        operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException(e.getMessage(), e);
            }
        } else if (CollectionUtils.isNotEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatementWithRequiredReturnFieldNames(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        tableListOfDfConfDataFieldDoInOrder,
                        mapOfSort,
                        sortListOfDfConfDataFieldDoInOrder,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        dfAdvancedQueryDto.getRequiredReturnFieldNames(),
                        pageable,
                        operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException(e.getMessage(), e);
            }
        } else if (dfAdvancedQueryDto.getGroupBy() != null) {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatementWithAdvancedGroupBy(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        dfAdvancedQueryDto.getGroupBy(),
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatementWithAdvancedGroupBy(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        tableListOfDfConfDataFieldDoInOrder,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        dfAdvancedQueryDto.getGroupBy(),
                        pageable,
                        operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException(e.getMessage(), e);
            }
        } else {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        tableListOfDfConfDataFieldDoInOrder,
                        mapOfSort,
                        sortListOfDfConfDataFieldDoInOrder,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        pageable,
                        operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException(e.getMessage(), e);
            }
        }

        QueryStatement queryStatement = new QueryStatement();
        queryStatement.setCountSqlStatement(countSqlStatement);
        queryStatement.setQuerySqlStatement(querySqlStatement);
        queryStatement.setDsType(dsDo.getType());
        queryStatement.setDsConnectionProfileProps(dsDo.getConnectionProfileProps());

        return queryStatement;
    }

    /**
     * 构造查询语句，含count语句和query语句，其中query语句不含分页从句
     *
     * @param dfKey
     * @param parameterMap
     * @param dfAdvancedQueryDto
     * @param sort
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public QueryStatement buildQueryStatementWithoutPagination(
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 找到df的定义
        // df定义
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }
        List<DfDataObjectDo> listOfDfDataObjectDo =
                this.dfDataObjectRepository.findByDfUidAndDataObjectDependencyType(
                        dfDo.getUid(), DataObjectDependencyTypeEnum.PRIMARY);
        if (CollectionUtils.isEmpty(listOfDfDataObjectDo)) {
            throw new ResourceNotFoundException(DfDataObjectDo.RESOURCE_NAME + ":" +
                    dfDo.getUid() + "," + DataObjectDependencyTypeEnum.PRIMARY);
        }
        DfDataObjectDo primaryDfDataObjectDo = listOfDfDataObjectDo.get(0);
        Long dataObjectUid = primaryDfDataObjectDo.getDataObjectUid();
        DsDataObjectDo dsDataObjectDo = this.dsDataObjectRepository.findByUid(dataObjectUid);
        if (dsDataObjectDo == null) {
            throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + dataObjectUid);
        }
        DsDo dsDo = this.dsRepository.findByUid(dsDataObjectDo.getDsUid());
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + dsDataObjectDo.getDsUid());
        }

        // Step 2, 取dictionary category and structures
        SimpleTreeNode allDictionaryStructures = this.dictionaryHandler.listAllDictionaryStructures(operationUserInfo);
        List<SimpleTreeNode> allDictionaryCategories = null;
        Map<Long, SimpleTreeNode> mapOfDictionaryCategoryTreeNode = new HashMap<>();
        if (allDictionaryStructures != null &&
                CollectionUtils.isNotEmpty(allDictionaryStructures.getChildren())) {
            allDictionaryCategories = allDictionaryStructures.getChildren();
            if (CollectionUtils.isNotEmpty(allDictionaryCategories)) {
                allDictionaryCategories.forEach(dictionaryCategoryTreeNode -> {
                    mapOfDictionaryCategoryTreeNode.put(dictionaryCategoryTreeNode.getUidCode(), dictionaryCategoryTreeNode);
                });
            }
        }

        // Step 3, 取df的form配置
        List<DfConfDataFieldDo> formListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findFormDataFieldsByDfUidAndOrderByFormElementSequenceAsc(dfDo.getUid());
        // key - field name, value - form data field
        Map<String, DfConfDataFieldDo> mapOfForm = new HashMap<>();
        // key - dictionary category id, value - first one of cascade fields
        Map<String, DfConfDataFieldDo> mapOfCascadeFields = new HashMap<>();
        if (CollectionUtils.isNotEmpty(formListOfDfConfDataFieldDoInOrder)) {
            formListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfForm.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);

                // 处理级联字段组合的第1个字段
                switch (dfConfDataFieldDo.getFormElementType()) {
                    case CASCADER:
                    case CASCADER_MULTIPLE:
                        Long dictionaryCategoryUid = dfConfDataFieldDo.getDictionaryCategoryUid();
                        if (dictionaryCategoryUid != null) {
                            if (mapOfDictionaryCategoryTreeNode.containsKey(dictionaryCategoryUid)) {
                                String dictionaryCategoryName = mapOfDictionaryCategoryTreeNode.get(dictionaryCategoryUid).getName();
                                if (Strings.isNotEmpty(dictionaryCategoryName)) {
                                    mapOfCascadeFields.put(dictionaryCategoryName, dfConfDataFieldDo);
                                }
                            }
                        }
                        break;
                }
            });
        } else {
            // 没有需要作为查询条件的字段
            // 允许继续查询，只是不需要查询条件
        }

        // Step 4, 取df的table配置
        List<DfConfDataFieldDo> tableListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(dfDo.getUid());
        Map<String, DfConfDataFieldDo> mapOfTable = new HashMap<>();
        if (CollectionUtils.isNotEmpty(tableListOfDfConfDataFieldDoInOrder)) {
            tableListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfTable.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
            });
        } else {
            // 没有需要list的字段
            // 查询就没有意义了，返回null
            LOGGER.warn("cannot find data fields enabled as list for df {}", dfDo.getUid());
            return null;
        }

        // Step 5, 取df的sort配置
        List<DfConfDataFieldDo> sortListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findSortDataFieldsByDfUidAndOrderBySortElementSequenceAsc(dfDo.getUid());
        Map<String, DfConfDataFieldDo> mapOfSort = new HashMap<>();
        if (CollectionUtils.isNotEmpty(sortListOfDfConfDataFieldDoInOrder)) {
            sortListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfSort.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
            });
        }

        // Step 5, 处理级联字段组合
        List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto = new ArrayList<>();
        Map<String, String[]> generalParameterMap = new HashMap<>(parameterMap.size());
        if (parameterMap != null && !parameterMap.isEmpty()) {
            parameterMap.keySet().forEach(parameterName -> {
                // 级联字段组合的参数名采用的是dictionary category name，不存在于form data fields中，需要特殊处理
                if (mapOfCascadeFields.containsKey(parameterName)) {
                    DfConfDataFieldDo dfConfDataFieldDo = mapOfCascadeFields.get(parameterName);

                    String[] arrayOfParameterValue = parameterMap.get(parameterName);

                    CascadeQueryParameterDto cascadeQueryParameterDto = new CascadeQueryParameterDto();
                    listOfCascadeQueryParameterDto.add(cascadeQueryParameterDto);
                    List<Map<String, String>> cascadeParameterMap = new ArrayList<>();
                    cascadeQueryParameterDto.setCascadeCollectionName(parameterName);
                    cascadeQueryParameterDto.setCollectionValues(cascadeParameterMap);

                    for (int i = 0; i < arrayOfParameterValue.length; i++) {
                        DfConfDataFieldDo specificDfConfDataFieldDo = dfConfDataFieldDo;
                        String parameterValue = arrayOfParameterValue[i];
                        String[] arrayOfSplitOfParameterValue = parameterValue.split(">");
                        cascadeParameterMap.add(new HashMap<>(arrayOfSplitOfParameterValue.length));
                        for (int j = 0; j < arrayOfSplitOfParameterValue.length; j++) {
                            if (specificDfConfDataFieldDo != null) {
                                cascadeParameterMap.get(i).put(specificDfConfDataFieldDo.getFieldName(), arrayOfSplitOfParameterValue[j]);
                                if (Strings.isNotEmpty(specificDfConfDataFieldDo.getChildFieldName())) {
                                    specificDfConfDataFieldDo = mapOfForm.get(specificDfConfDataFieldDo.getChildFieldName());
                                }
                            } else {
                                LOGGER.warn("null df conf data field, i=" + i + ", j=" + j);
                            }
                        }
                    }
                } else {
                    generalParameterMap.put(parameterName, parameterMap.get(parameterName));
                }
            });
        }

        // Step 6, 校验基础请求参数
        if (generalParameterMap != null && !generalParameterMap.isEmpty()) {
            generalParameterMap.keySet().forEach(parameterName -> {
                if (parameterMap.get(parameterName).length > 2000) {
                    throw new IllegalParameterException("found " + parameterMap.get(parameterName).length +
                            " parameter values for " + parameterName);
                }
            });
        }

        // Step 7, 将请求参数翻译成SQL语句
        String countSqlStatement = null;
        String querySqlStatement = null;
        if (dfAdvancedQueryDto == null) {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatementWithoutPagination(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        tableListOfDfConfDataFieldDoInOrder,
                        mapOfSort,
                        sortListOfDfConfDataFieldDoInOrder,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        sort,
                        operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException("failed to build query statement. " + e.getMessage(), e);
            }
        } else if (CollectionUtils.isNotEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement =
                        this.sqlTranslationHandler.buildQuerySqlStatementWithRequiredReturnFieldNamesWithoutPagination(
                                dsDo.getType(),
                                dfDo.getProcessingNeeded(),
                                dfDo.getProcessingLogic(),
                                dsDataObjectDo.getDbName(),
                                dsDataObjectDo.getSchemaName(),
                                dsDataObjectDo.getDataObjectName(),
                                mapOfForm,
                                mapOfTable,
                                tableListOfDfConfDataFieldDoInOrder,
                                mapOfSort,
                                sortListOfDfConfDataFieldDoInOrder,
                                listOfCascadeQueryParameterDto,
                                generalParameterMap,
                                dfAdvancedQueryDto.getRequiredReturnFieldNames(),
                                sort,
                                operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException("failed to build query statement. " + e.getMessage(), e);
            }
        } else if (dfAdvancedQueryDto.getGroupBy() != null) {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatementWithAdvancedGroupBy(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        dfAdvancedQueryDto.getGroupBy(),
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatementWithAdvancedGroupByWithoutPagination(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        tableListOfDfConfDataFieldDoInOrder,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        dfAdvancedQueryDto.getGroupBy(),
                        sort,
                        operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException("failed to build query statement. " + e.getMessage(), e);
            }
        } else {
            try {
                // 翻译成count sql语句
                countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatement(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        operationUserInfo);

                // 翻译成query sql语句
                querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatementWithoutPagination(
                        dsDo.getType(),
                        dfDo.getProcessingNeeded(),
                        dfDo.getProcessingLogic(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName(),
                        mapOfForm,
                        mapOfTable,
                        tableListOfDfConfDataFieldDoInOrder,
                        mapOfSort,
                        sortListOfDfConfDataFieldDoInOrder,
                        listOfCascadeQueryParameterDto,
                        generalParameterMap,
                        sort,
                        operationUserInfo);
            } catch (Exception e) {
                throw new ServiceException("failed to build query statement. " + e.getMessage(), e);
            }
        }

        QueryStatement queryStatement = new QueryStatement();
        queryStatement.setCountSqlStatement(countSqlStatement);
        queryStatement.setQuerySqlStatement(querySqlStatement);
        queryStatement.setDsType(dsDo.getType());
        queryStatement.setDsConnectionProfileProps(dsDo.getConnectionProfileProps());

        return queryStatement;
    }

    /**
     * 构造Group By查询语句，含query语句，其中query语句不含分页从句
     *
     * @param dfKey
     * @param dimensionFieldNames
     * @param kpiFieldName
     * @param parameterMap
     * @param sort
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public QueryStatement buildGroupByQueryStatement(
            String dfKey,
            List<String> dimensionFieldNames,
            String kpiFieldName,
            Map<String, String[]> parameterMap,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 找到df的定义
        // df定义
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }
        List<DfDataObjectDo> listOfDfDataObjectDo =
                this.dfDataObjectRepository.findByDfUidAndDataObjectDependencyType(
                        dfDo.getUid(), DataObjectDependencyTypeEnum.PRIMARY);
        if (CollectionUtils.isEmpty(listOfDfDataObjectDo)) {
            throw new ResourceNotFoundException(DfDataObjectDo.RESOURCE_NAME + ":" +
                    dfDo.getUid() + "," + DataObjectDependencyTypeEnum.PRIMARY);
        }
        DfDataObjectDo primaryDfDataObjectDo = listOfDfDataObjectDo.get(0);
        Long dataObjectUid = primaryDfDataObjectDo.getDataObjectUid();
        DsDataObjectDo dsDataObjectDo = this.dsDataObjectRepository.findByUid(dataObjectUid);
        if (dsDataObjectDo == null) {
            throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + dataObjectUid);
        }
        DsDo dsDo = this.dsRepository.findByUid(dsDataObjectDo.getDsUid());
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + dsDataObjectDo.getDsUid());
        }

        // Step 2, 取dictionary category and structures
        SimpleTreeNode allDictionaryStructures = this.dictionaryHandler.listAllDictionaryStructures(operationUserInfo);
        List<SimpleTreeNode> allDictionaryCategories = null;
        Map<Long, SimpleTreeNode> mapOfDictionaryCategoryTreeNode = new HashMap<>();
        if (allDictionaryStructures != null &&
                CollectionUtils.isNotEmpty(allDictionaryStructures.getChildren())) {
            allDictionaryCategories = allDictionaryStructures.getChildren();
            if (CollectionUtils.isNotEmpty(allDictionaryCategories)) {
                allDictionaryCategories.forEach(dictionaryCategoryTreeNode -> {
                    mapOfDictionaryCategoryTreeNode.put(dictionaryCategoryTreeNode.getUidCode(), dictionaryCategoryTreeNode);
                });
            }
        }

        // Step 3, 取df的form配置
        List<DfConfDataFieldDo> formListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findFormDataFieldsByDfUidAndOrderByFormElementSequenceAsc(dfDo.getUid());
        // key - field name, value - form data field
        Map<String, DfConfDataFieldDo> mapOfForm = new HashMap<>();
        // key - dictionary category id, value - first one of cascade fields
        Map<String, DfConfDataFieldDo> mapOfCascadeFields = new HashMap<>();
        if (CollectionUtils.isNotEmpty(formListOfDfConfDataFieldDoInOrder)) {
            formListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfForm.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);

                // 处理级联字段组合的第1个字段
                switch (dfConfDataFieldDo.getFormElementType()) {
                    case CASCADER:
                    case CASCADER_MULTIPLE:
                        Long dictionaryCategoryUid = dfConfDataFieldDo.getDictionaryCategoryUid();
                        if (dictionaryCategoryUid != null) {
                            if (mapOfDictionaryCategoryTreeNode.containsKey(dictionaryCategoryUid)) {
                                String dictionaryCategoryName = mapOfDictionaryCategoryTreeNode.get(dictionaryCategoryUid).getName();
                                if (Strings.isNotEmpty(dictionaryCategoryName)) {
                                    mapOfCascadeFields.put(dictionaryCategoryName, dfConfDataFieldDo);
                                }
                            }
                        }
                        break;
                }
            });
        } else {
            // 没有需要作为查询条件的字段
            // 允许继续查询，只是不需要查询条件
        }

        // Step 4, 取df的table配置
        List<DfConfDataFieldDo> tableListOfDfConfDataFieldDoInOrder =
                this.dfConfDataFieldRepository.findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(dfDo.getUid());
        Map<String, DfConfDataFieldDo> mapOfTable = new HashMap<>();
        if (CollectionUtils.isNotEmpty(tableListOfDfConfDataFieldDoInOrder)) {
            tableListOfDfConfDataFieldDoInOrder.forEach(dfConfDataFieldDo -> {
                mapOfTable.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
            });
        } else {
            // 没有需要list的字段
            // 查询就没有意义了，返回null
            LOGGER.warn("cannot find data fields enabled as list for df {}", dfDo.getUid());
            return null;
        }

        // Step 5, 处理级联字段组合
        List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto = new ArrayList<>();
        Map<String, String[]> generalParameterMap = new HashMap<>(parameterMap.size());
        if (parameterMap != null && !parameterMap.isEmpty()) {
            parameterMap.keySet().forEach(parameterName -> {
                // 级联字段组合的参数名采用的是dictionary category name，不存在于form data fields中，需要特殊处理
                if (mapOfCascadeFields.containsKey(parameterName)) {
                    DfConfDataFieldDo dfConfDataFieldDo = mapOfCascadeFields.get(parameterName);

                    String[] arrayOfParameterValue = parameterMap.get(parameterName);

                    CascadeQueryParameterDto cascadeQueryParameterDto = new CascadeQueryParameterDto();
                    listOfCascadeQueryParameterDto.add(cascadeQueryParameterDto);
                    List<Map<String, String>> cascadeParameterMap = new ArrayList<>();
                    cascadeQueryParameterDto.setCascadeCollectionName(parameterName);
                    cascadeQueryParameterDto.setCollectionValues(cascadeParameterMap);

                    for (int i = 0; i < arrayOfParameterValue.length; i++) {
                        DfConfDataFieldDo specificDfConfDataFieldDo = dfConfDataFieldDo;
                        String parameterValue = arrayOfParameterValue[i];
                        String[] arrayOfSplitOfParameterValue = parameterValue.split(">");
                        cascadeParameterMap.add(new HashMap<>(arrayOfSplitOfParameterValue.length));
                        for (int j = 0; j < arrayOfSplitOfParameterValue.length; j++) {
                            if (specificDfConfDataFieldDo != null) {
                                cascadeParameterMap.get(i).put(specificDfConfDataFieldDo.getFieldName(), arrayOfSplitOfParameterValue[j]);
                                if (Strings.isNotEmpty(specificDfConfDataFieldDo.getChildFieldName())) {
                                    specificDfConfDataFieldDo = mapOfForm.get(specificDfConfDataFieldDo.getChildFieldName());
                                }
                            } else {
                                LOGGER.warn("null df conf data field, i=" + i + ", j=" + j);
                            }
                        }
                    }
                } else {
                    generalParameterMap.put(parameterName, parameterMap.get(parameterName));
                }
            });
        }

        // Step 6, 校验基础请求参数
        if (generalParameterMap != null && !generalParameterMap.isEmpty()) {
            generalParameterMap.keySet().forEach(parameterName -> {
                if (!mapOfForm.containsKey(parameterName)) {
                    throw new IllegalParameterException("form request field name " + parameterName + " does not exist");
                }
                if (parameterMap.get(parameterName).length > 2000) {
                    throw new IllegalParameterException("found " + parameterMap.get(parameterName).length +
                            " parameter values for " + parameterName);
                }
            });
        }

        // Step 6.1, 更多校验
        DfConfDataFieldDo kpiDfConfDataFieldDo = mapOfTable.get(kpiFieldName);
        if (kpiDfConfDataFieldDo == null) {
            throw new IllegalParameterException("cannot find kpi field name " + kpiFieldName);
        }
        boolean validKpiField = false;
        switch (kpiDfConfDataFieldDo.getFieldType()) {
            case INT:
            case LONG:
            case DECIMAL:
                validKpiField = true;
                break;
        }
        if (!validKpiField) {
            throw new IllegalParameterException("invalid kpi field " + kpiFieldName + ", because it is NOT a number " +
                    "field");
        }

        // Step 7, 将请求参数翻译成SQL语句
        String countSqlStatement = null;
        String querySqlStatement = null;
        try {
            // 翻译成count sql语句
            countSqlStatement = this.sqlTranslationHandler.buildCountSqlStatementWithBasicGroupBy(
                    dsDo.getType(),
                    dfDo.getProcessingNeeded(),
                    dfDo.getProcessingLogic(),
                    dsDataObjectDo.getDbName(),
                    dsDataObjectDo.getSchemaName(),
                    dsDataObjectDo.getDataObjectName(),
                    mapOfForm,
                    mapOfTable,
                    listOfCascadeQueryParameterDto,
                    generalParameterMap,
                    dimensionFieldNames,
                    kpiFieldName,
                    operationUserInfo);


            // 翻译成query sql语句
            querySqlStatement = this.sqlTranslationHandler.buildQuerySqlStatementWithBasicGroupByWithoutPagination(
                    dsDo.getType(),
                    dfDo.getProcessingNeeded(),
                    dfDo.getProcessingLogic(),
                    dsDataObjectDo.getDbName(),
                    dsDataObjectDo.getSchemaName(),
                    dsDataObjectDo.getDataObjectName(),
                    mapOfForm,
                    mapOfTable,
                    listOfCascadeQueryParameterDto,
                    generalParameterMap,
                    dimensionFieldNames,
                    kpiFieldName,
                    sort,
                    operationUserInfo);
        } catch (Exception e) {
            throw new ServiceException("failed to build query statement. " + e.getMessage(), e);
        }

        QueryStatement queryStatement = new QueryStatement();
        queryStatement.setCountSqlStatement(countSqlStatement);
        queryStatement.setQuerySqlStatement(querySqlStatement);
        queryStatement.setDsType(dsDo.getType());
        queryStatement.setDsConnectionProfileProps(dsDo.getConnectionProfileProps());

        return queryStatement;
    }

    public static class QueryStatement {
        private String countSqlStatement;
        private String querySqlStatement;
        private DsTypeEnum dsType;
        private String dsConnectionProfileProps;

        public String getCountSqlStatement() {
            return countSqlStatement;
        }

        public void setCountSqlStatement(String countSqlStatement) {
            this.countSqlStatement = countSqlStatement;
        }

        public String getQuerySqlStatement() {
            return querySqlStatement;
        }

        public void setQuerySqlStatement(String querySqlStatement) {
            this.querySqlStatement = querySqlStatement;
        }

        public DsTypeEnum getDsType() {
            return dsType;
        }

        public void setDsType(DsTypeEnum dsType) {
            this.dsType = dsType;
        }

        public String getDsConnectionProfileProps() {
            return dsConnectionProfileProps;
        }

        public void setDsConnectionProfileProps(String dsConnectionProfileProps) {
            this.dsConnectionProfileProps = dsConnectionProfileProps;
        }
    }
}
