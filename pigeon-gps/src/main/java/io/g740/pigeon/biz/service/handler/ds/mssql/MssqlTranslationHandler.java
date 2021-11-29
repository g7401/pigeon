package io.g740.pigeon.biz.service.handler.ds.mssql;

import com.google.common.base.Strings;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.CascadeQueryParameterDto;
import io.g740.pigeon.biz.object.dto.df.DfQueryGroupByDto;
import io.g740.pigeon.biz.object.dto.df.DfQueryHavingFieldDto;
import io.g740.pigeon.biz.object.dto.df.DfQueryResultFieldDto;
import io.g740.pigeon.biz.object.entity.df.DfConfDataFieldDo;
import io.g740.pigeon.biz.share.constants.DataFieldRoleEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * @author bbottong
 */
@Handler
public class MssqlTranslationHandler {

    /**
     * 构建基本计数语句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param operationUserInfo
     * @return
     */
    public String buildCountSqlStatement(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        // Step 1, select clause
        sql.append("SELECT COUNT(*) as cnt FROM");
        if (processingNeeded) {
            sql.append(" " + "(" + processingLogic + ") ta");
        } else {
            sql.append(" [" + dbName + "].[" + schemaName + "].[" + dataObjectName + "]");
        }

        // Step 2, where clause
        sql.append(" WHERE 1=1");
        sql.append(buildWhereClause(listOfCascadeQueryParameter, generalParameterMap, mapOfForm));

        return sql.toString();
    }

    /**
     * 构造基本查询语句，含分页从句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param mapOfTable
     * @param listOfTableInOrder
     * @param mapOfSort
     * @param listOfSortInOrder
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param pageable
     * @param operationUserInfo
     * @return
     */
    public String buildQuerySqlStatement(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            Map<String, DfConfDataFieldDo> mapOfSort,
            List<DfConfDataFieldDo> listOfSortInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            Pageable pageable,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        String querySqlWithoutPagination = buildQuerySqlStatementWithoutPagination(
                processingNeeded,
                processingLogic,
                dbName,
                schemaName,
                dataObjectName,
                mapOfForm,
                mapOfTable,
                listOfTableInOrder,
                mapOfSort,
                listOfSortInOrder,
                listOfCascadeQueryParameter,
                generalParameterMap,
                pageable.getSort(),
                operationUserInfo);
        sql.append(querySqlWithoutPagination);

        // pagination clause
        sql.append(" OFFSET " + pageable.getOffset());
        sql.append(" ROWS FETCH NEXT " + pageable.getPageSize());
        sql.append(" ROWS ONLY");

        return sql.toString();
    }

    /**
     * 构造基本查询语句，不含分页从句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param mapOfTable
     * @param listOfTableInOrder
     * @param mapOfSort
     * @param listOfSortInOrder
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param sort
     * @param operationUserInfo
     * @return
     */
    public String buildQuerySqlStatementWithoutPagination(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            Map<String, DfConfDataFieldDo> mapOfSort,
            List<DfConfDataFieldDo> listOfSortInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            Sort sort,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        // Step 1, select clause
        sql.append("USE [" + dbName + "];");
        sql.append("SELECT ");
        mapOfTable.keySet().forEach(fieldName -> {
            sql.append("[" + buildEscapedName(fieldName) + "]" + ",");
        });
        sql.deleteCharAt(sql.length() - 1);

        sql.append(" FROM");
        if (processingNeeded) {
            sql.append(" " + "(" + processingLogic + ") ta");
        } else {
            sql.append(" [" + dbName + "].[" + schemaName + "].[" + dataObjectName + "]");
        }

        // Step 2, where clause
        sql.append(" WHERE 1=1");
        sql.append(buildWhereClause(listOfCascadeQueryParameter, generalParameterMap, mapOfForm));

        // Step 3, order by clause
        if (CollectionUtils.isNotEmpty(listOfSortInOrder)) {
            StringBuilder sortClause = new StringBuilder();
            listOfSortInOrder.forEach(dfConfDataFieldDo -> {
                boolean found = false;
                if (sort != null && sort.isSorted()) {
                    for (Sort.Order order : sort) {
                        if (dfConfDataFieldDo.getFieldName().equals(order.getProperty())) {
                            found = true;
                            sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + order.getDirection().name() + ",");
                            break;
                        }
                    }
                }
                if (!found) {
                    if (dfConfDataFieldDo.getSortDirection() != null) {
                        sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + dfConfDataFieldDo.getSortDirection().name() + ",");
                    } else {
                        sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + DfConfDataFieldDo.DEFAULT_SORT_ORDER_DIRECTION + ",");
                    }
                }
            });
            if (sortClause.length() > 0) {
                sortClause.deleteCharAt(sortClause.length() - 1);
                sql.append(" ORDER BY ");
                sql.append(sortClause);
            }
        } else {
            // MSSQL 的OFFSET FETCH分页从句必须要有ORDER BY，按sequence由小到大选择ORDER BY
            StringBuilder sortClause = new StringBuilder();
            for (DfConfDataFieldDo dfConfDataFieldDo : listOfTableInOrder) {
                sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + DfConfDataFieldDo.DEFAULT_SORT_ORDER_DIRECTION + ",");
            }
            if (sortClause.length() > 0) {
                sortClause.deleteCharAt(sortClause.length() - 1);
                sql.append(" ORDER BY ");
                sql.append(sortClause);
            }
        }

        return sql.toString();
    }

    /**
     * 添加分页从句
     *
     * @param querySqlStatement
     * @param pageNumber
     * @param pageSize
     * @param operationUserInfo
     * @return
     */
    public String addPagination(
            String querySqlStatement,
            int pageNumber,
            int pageSize,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        sql.append(querySqlStatement);

        // pagination clause
        sql.append(" OFFSET " + pageNumber * pageSize);
        sql.append(" ROWS FETCH NEXT " + pageSize);
        sql.append(" ROWS ONLY");

        return sql.toString();
    }

    /**
     * 构造指定了返回字段的基本查询语句，含分页从句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param mapOfTable
     * @param listOfTableInOrder
     * @param mapOfSort
     * @param listOfSortInOrder
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param requiredReturnFieldNames
     * @param pageable
     * @param operationUserInfo
     * @return
     */
    public String buildQuerySqlStatementWithRequiredReturnFieldNames(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            Map<String, DfConfDataFieldDo> mapOfSort,
            List<DfConfDataFieldDo> listOfSortInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            List<String> requiredReturnFieldNames,
            Pageable pageable,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        String querySqlWithoutPagination = buildQuerySqlStatementWithRequiredReturnFieldNamesWithoutPagination(
                processingNeeded,
                processingLogic,
                dbName,
                schemaName,
                dataObjectName,
                mapOfForm,
                mapOfTable,
                listOfTableInOrder,
                mapOfSort,
                listOfSortInOrder,
                listOfCascadeQueryParameter,
                generalParameterMap,
                requiredReturnFieldNames,
                pageable.getSort(),
                operationUserInfo);
        sql.append(querySqlWithoutPagination);

        // pagination clause
        sql.append(" OFFSET " + pageable.getOffset());
        sql.append(" ROWS FETCH NEXT " + pageable.getPageSize());
        sql.append(" ROWS ONLY");

        return sql.toString();
    }

    /**
     * 构造指定了返回字段的基本查询语句，不含分页从句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param mapOfTable
     * @param listOfTableInOrder
     * @param mapOfSort
     * @param listOfSortInOrder
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param requiredReturnFieldNames
     * @param sort
     * @param operationUserInfo
     * @return
     */
    public String buildQuerySqlStatementWithRequiredReturnFieldNamesWithoutPagination(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            Map<String, DfConfDataFieldDo> mapOfSort,
            List<DfConfDataFieldDo> listOfSortInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            List<String> requiredReturnFieldNames,
            Sort sort,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        // Step 1, select clause
        sql.append("USE [" + dbName + "];");
        sql.append("SELECT ");
        for (int i = 0; i < requiredReturnFieldNames.size(); i++) {
            if (i == 0) {
                sql.append("[" + buildEscapedName(requiredReturnFieldNames.get(i)) + "]");
            } else {
                sql.append("," + "[" + buildEscapedName(requiredReturnFieldNames.get(i)) + "]");
            }
        }
        sql.append(" FROM");
        if (processingNeeded) {
            sql.append(" " + "(" + processingLogic + ") ta");
        } else {
            sql.append(" [" + dbName + "].[" + schemaName + "].[" + dataObjectName + "]");
        }

        // Step 2, where clause
        sql.append(" WHERE 1=1");
        sql.append(buildWhereClause(listOfCascadeQueryParameter, generalParameterMap, mapOfForm));

        // Step 3, order by clause
        if (CollectionUtils.isNotEmpty(listOfSortInOrder)) {
            StringBuilder sortClause = new StringBuilder();
            listOfSortInOrder.forEach(dfConfDataFieldDo -> {
                boolean found = false;
                if (sort != null && sort.isSorted()) {
                    for (Sort.Order order : sort) {
                        if (dfConfDataFieldDo.getFieldName().equals(order.getProperty())) {
                            found = true;
                            sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + order.getDirection().name() + ",");
                            break;
                        }
                    }
                }
                if (!found) {
                    if (dfConfDataFieldDo.getSortDirection() != null) {
                        sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + dfConfDataFieldDo.getSortDirection().name() + ",");
                    } else {
                        sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + DfConfDataFieldDo.DEFAULT_SORT_ORDER_DIRECTION + ",");
                    }
                }
            });
            if (sortClause.length() > 0) {
                sortClause.deleteCharAt(sortClause.length() - 1);
                sql.append(" ORDER BY ");
                sql.append(sortClause);
            }
        } else {
            // MSSQL 的OFFSET FETCH分页从句必须要有ORDER BY，按sequence由小到大选择ORDER BY
            StringBuilder sortClause = new StringBuilder();
            for (DfConfDataFieldDo dfConfDataFieldDo : listOfTableInOrder) {
                sortClause.append("[" + buildEscapedName(dfConfDataFieldDo.getFieldName()) + "]" + " " + DfConfDataFieldDo.DEFAULT_SORT_ORDER_DIRECTION + ",");
            }
            if (sortClause.length() > 0) {
                sortClause.deleteCharAt(sortClause.length() - 1);
                sql.append(" ORDER BY ");
                sql.append(sortClause);
            }
        }


        return sql.toString();
    }

    /**
     * 构造指定了"高级GROUP BY"字段的计数语句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param mapOfTable
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param dfQueryGroupByDto
     * @param operationUserInfo
     * @return
     */
    public String buildCountSqlStatementWithAdvancedGroupBy(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            DfQueryGroupByDto dfQueryGroupByDto,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        // Step 1, 1st level select

        // Step 1.1 select count
        sql.append("SELECT");
        sql.append(" COUNT(*) as cnt");
        sql.append(" FROM");

        // Step 1.2, 2nd level select start
        sql.append(" " + "(");

        // 包含可能的select从句，条件从句，group by从句，having从句
        //
        // SELECT column_name, aggregate_function(column_name)
        // FROM table_name
        // WHERE column_name operator value
        // GROUP BY column_name
        // HAVING aggregate_function(column_name) operator value
        //

        // Step 1.2.1, select clause
        sql.append(" SELECT ");
        switch (dfQueryGroupByDto.getFieldProcessingType()) {
            case RETURN_ONLY_SELECTED_FIELDS:
            case RETURN_ALL_FIELDS:
                // 针对RETURN_ONLY_SELECTED_FIELDS 和 RETURN_ALL_FIELDS，
                // 都不用把全部字段都构造出来，这里只是用来统计行数
                for (String selectedGroupByFieldName : dfQueryGroupByDto.getSelectedGroupByFieldNames()) {
                    sql.append("[" + buildEscapedName(selectedGroupByFieldName) + "]" + ",");
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
            case ADVANCED:
                for (DfQueryResultFieldDto resultField : dfQueryGroupByDto.getResultFields()) {
                    sql.append(buildSelectField(resultField, mapOfTable) + ",");
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
        }

        // Step 1.2.2, from clause
        sql.append(" FROM ");
        if (processingNeeded) {
            sql.append(" " + "(" + processingLogic + ") ta");
        } else {
            sql.append(" [" + dbName + "].[" + schemaName + "].[" + dataObjectName + "]");
        }

        // Step 1.2.3, where clause
        sql.append(" WHERE 1=1 ");
        sql.append(buildWhereClause(listOfCascadeQueryParameter, generalParameterMap, mapOfForm));

        // Step 1.2.4, group by clause
        sql.append(" GROUP BY ");
        for (String fieldName : dfQueryGroupByDto.getSelectedGroupByFieldNames()) {
            sql.append("[" + buildEscapedName(fieldName) + "]" + ",");
        }
        sql.deleteCharAt(sql.length() - 1);

        // Step 1.2.5, having clause
        if (CollectionUtils.isNotEmpty(dfQueryGroupByDto.getHavingFields())) {
            // having从句中的new field label需要利用result fields的new field label找到field name
            Map<String, DfQueryResultFieldDto> mapOfResultFieldWithAggregateFunction = new HashMap<>();
            if (CollectionUtils.isNotEmpty(dfQueryGroupByDto.getResultFields())) {
                for (DfQueryResultFieldDto field : dfQueryGroupByDto.getResultFields()) {
                    if (field.getAggregateFunctionType() != null) {
                        mapOfResultFieldWithAggregateFunction.put(field.getNewFieldLabel(), field);
                    }
                }
            }

            sql.append(" HAVING ");
            for (int i = 0; i < dfQueryGroupByDto.getHavingFields().size(); i++) {
                DfQueryHavingFieldDto dfQueryHavingFieldDto = dfQueryGroupByDto.getHavingFields().get(i);
                DfQueryResultFieldDto dfQueryResultFieldDto = mapOfResultFieldWithAggregateFunction.get(dfQueryHavingFieldDto.getNewFieldLabel());
                String havingCondition = buildHavingCondition(dfQueryResultFieldDto, dfQueryHavingFieldDto, mapOfTable);
                if (i == 0) {
                    sql.append(havingCondition);
                } else {
                    sql.append(" AND " + havingCondition);
                }
            }
        }

        // Step 1.3,  2nd level select end
        sql.append(") tb");

        return sql.toString();
    }

    /**
     * 构造指定了"高级GROUP BY"字段的查询语句，含分页从句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param mapOfTable
     * @param listOfTableInOrder
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param dfQueryGroupByDto
     * @param pageable
     * @param operationUserInfo
     * @return
     */
    public String buildQuerySqlStatementWithAdvancedGroupBy(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            DfQueryGroupByDto dfQueryGroupByDto,
            Pageable pageable,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        String querySqlWithoutPagination = buildQuerySqlStatementWithAdvancedGroupByWithoutPagination(
                processingNeeded,
                processingLogic,
                dbName,
                schemaName,
                dataObjectName,
                mapOfForm,
                mapOfTable,
                listOfTableInOrder,
                listOfCascadeQueryParameter,
                generalParameterMap,
                dfQueryGroupByDto,
                operationUserInfo);
        sql.append(querySqlWithoutPagination);

        // pagination clause
        sql.append(" OFFSET " + pageable.getOffset());
        sql.append(" ROWS FETCH NEXT " + pageable.getPageSize());
        sql.append(" ROWS ONLY");

        return sql.toString();
    }

    /**
     * 构造指定了"高级GROUP BY"字段的查询语句，不含分页从句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param mapOfTable
     * @param listOfTableInOrder
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param dfQueryGroupByDto
     * @param operationUserInfo
     * @return
     */
    public String buildQuerySqlStatementWithAdvancedGroupByWithoutPagination(
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
            Map<String, String[]> generalParameterMap,
            DfQueryGroupByDto dfQueryGroupByDto,
            UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        // Step 1, 1st level select，处理排序条件
        sql.append("USE [" + dbName + "];");
        sql.append("SELECT ");

        switch (dfQueryGroupByDto.getFieldProcessingType()) {
            case RETURN_ONLY_SELECTED_FIELDS:
                // 显示两类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，默认采用配置的KPI聚合函数统计；
                for (String selectedGroupByFieldName : dfQueryGroupByDto.getSelectedGroupByFieldNames()) {
                    sql.append("[" + buildEscapedName(selectedGroupByFieldName) + "]" + ",");
                }
                for (DfConfDataFieldDo field : listOfTableInOrder) {
                    if (DataFieldRoleEnum.KPI.equals(field.getRole())) {
                        sql.append("[" + buildEscapedName(field.getFieldName()) + "]" + ",");
                    }
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
            case RETURN_ALL_FIELDS:
                // 显示三类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，默认采用配置的KPI聚合函数统计；3）剩余字段，默认返回空值；
                for (DfConfDataFieldDo field : listOfTableInOrder) {
                    sql.append("[" + buildEscapedName(field.getFieldName()) + "]" + ",");
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
            case ADVANCED:
                // 高级
                for (DfQueryResultFieldDto resultField : dfQueryGroupByDto.getResultFields()) {
                    if (Strings.isNullOrEmpty(resultField.getNewFieldLabel())) {
                        sql.append("[" + buildEscapedName(resultField.getFieldName()) + "]" + ",");
                    } else {
                        sql.append("[" + buildEscapedName(resultField.getNewFieldLabel()) + "]" + ",");
                    }
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
        }

        // Step 1.1, 1st level from clause
        sql.append(" FROM ");

        // Step 2, 2nd level select clause，处理查询条件
        // SELECT column_name, aggregate_function(column_name)
        // FROM table_name
        // WHERE column_name operator value
        // GROUP BY column_name
        // HAVING aggregate_function(column_name) operator value
        //

        // Step 2.1, 2nd level select start
        sql.append("(");

        // Step 2.2, select clause
        sql.append("SELECT ");
        switch (dfQueryGroupByDto.getFieldProcessingType()) {
            case RETURN_ONLY_SELECTED_FIELDS:
                // 显示两类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，默认采用配置的KPI聚合函数统计；
                for (String selectedGroupByFieldName : dfQueryGroupByDto.getSelectedGroupByFieldNames()) {
                    // 维度字段
                    sql.append("[" + buildEscapedName(selectedGroupByFieldName) + "]" + ",");
                }
                for (DfConfDataFieldDo field : listOfTableInOrder) {
                    if (DataFieldRoleEnum.KPI.equals(field.getRole())) {
                        // KPI字段
                        if (field.getAggregationType() == null) {
                            // 默认SUM
                            sql.append(buildClauseForAggregationTypeSum(field)).append(",");
                        } else {
                            switch (field.getAggregationType()) {
                                case SUM:
                                    sql.append(buildClauseForAggregationTypeSum(field)).append(",");
                                    break;
                                case AVG:
                                    sql.append(buildClauseForAggregationTypeAvg(field)).append(",");
                                    break;
                                case MIN:
                                    sql.append(buildClauseForAggregationTypeMin(field)).append(",");
                                    break;
                                case MAX:
                                    sql.append(buildClauseForAggregationTypeMax(field)).append(",");
                                    break;
                                case COUNT:
                                    sql.append(buildClauseForAggregationTypeCount(field)).append(",");
                                    break;
                                default:
                                    throw new RuntimeException("unsupported aggregation type");
                            }
                        }
                    }
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
            case RETURN_ALL_FIELDS:
                // 显示三类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，默认采用配置的KPI聚合函数统计；3）剩余字段，默认返回空值；
                for (DfConfDataFieldDo field : listOfTableInOrder) {
                    if (dfQueryGroupByDto.getSelectedGroupByFieldNames().contains(field.getFieldName())) {
                        // 维度字段
                        sql.append("[" + buildEscapedName(field.getFieldName()) + "]" + ",");
                    } else {
                        if (DataFieldRoleEnum.KPI.equals(field.getRole())) {
                            // KPI字段
                            if (field.getAggregationType() == null) {
                                // 默认SUM
                                sql.append(buildClauseForAggregationTypeSum(field)).append(",");
                            } else {
                                switch (field.getAggregationType()) {
                                    case SUM:
                                        sql.append(buildClauseForAggregationTypeSum(field)).append(",");
                                        break;
                                    case AVG:
                                        sql.append(buildClauseForAggregationTypeAvg(field)).append(",");
                                        break;
                                    case MIN:
                                        sql.append(buildClauseForAggregationTypeMin(field)).append(",");
                                        break;
                                    case MAX:
                                        sql.append(buildClauseForAggregationTypeMax(field)).append(",");
                                        break;
                                    case COUNT:
                                        sql.append(buildClauseForAggregationTypeCount(field)).append(",");
                                        break;
                                    default:
                                        throw new RuntimeException("unsupported aggregation type");
                                }
                            }
                        } else {
                            // 其它字段
                            sql.append("''" + " AS [" + buildEscapedName(field.getFieldName()) + "]" + ",");
                        }
                    }
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
            case ADVANCED:
                // 维度字段 和 KPI字段都已经在result fields
                for (DfQueryResultFieldDto resultField : dfQueryGroupByDto.getResultFields()) {
                    sql.append(buildSelectField(resultField, mapOfTable) + ",");
                }
                sql.deleteCharAt(sql.length() - 1);
                break;
        }

        // Step 2.3, from clause
        sql.append(" FROM ");
        if (processingNeeded) {
            sql.append(" " + "(" + processingLogic + ") ta");
        } else {
            sql.append(" [" + dbName + "].[" + schemaName + "].[" + dataObjectName + "]");
        }

        // Step 2.4, where clause
        sql.append(" WHERE 1=1 ");
        sql.append(buildWhereClause(listOfCascadeQueryParameter, generalParameterMap, mapOfForm));

        // Step 2.5, group by clause
        sql.append(" GROUP BY ");
        for (String fieldName : dfQueryGroupByDto.getSelectedGroupByFieldNames()) {
            sql.append("[" + buildEscapedName(fieldName) + "]" + ",");
        }
        sql.deleteCharAt(sql.length() - 1);

        // Step 2.6, having clause
        if (CollectionUtils.isNotEmpty(dfQueryGroupByDto.getHavingFields())) {
            // having从句中的new field label需要利用result fields的new field label找到field name
            Map<String, DfQueryResultFieldDto> mapOfResultFieldWithAggregateFunction = new HashMap<>();
            if (CollectionUtils.isNotEmpty(dfQueryGroupByDto.getResultFields())) {
                for (DfQueryResultFieldDto field : dfQueryGroupByDto.getResultFields()) {
                    if (field.getAggregateFunctionType() != null) {
                        mapOfResultFieldWithAggregateFunction.put(field.getNewFieldLabel(), field);
                    }
                }
            }

            sql.append(" HAVING ");
            for (int i = 0; i < dfQueryGroupByDto.getHavingFields().size(); i++) {
                DfQueryHavingFieldDto dfQueryHavingFieldDto = dfQueryGroupByDto.getHavingFields().get(i);
                DfQueryResultFieldDto dfQueryResultFieldDto = mapOfResultFieldWithAggregateFunction.get(dfQueryHavingFieldDto.getNewFieldLabel());
                String havingCondition = buildHavingCondition(dfQueryResultFieldDto, dfQueryHavingFieldDto, mapOfTable);
                if (i == 0) {
                    sql.append(havingCondition);
                } else {
                    sql.append(" AND " + havingCondition);
                }
            }
        }

        // Step 2.7, 2nd level select end
        sql.append(") tb ");

        // Step 3, order by clause
        StringBuilder orderByClause = new StringBuilder();
        switch (dfQueryGroupByDto.getFieldProcessingType()) {
            case RETURN_ONLY_SELECTED_FIELDS:
                // 显示两类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，且默认采用加和统计；
            case RETURN_ALL_FIELDS:
                // 显示三类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，且默认采用加和统计；3）剩余字段，默认返回空值；
            case ADVANCED:
                // 显示三类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，默认采用配置的KPI聚合函数统计；3）剩余字段，默认返回空值；
                for (String selectedGroupByFieldName : dfQueryGroupByDto.getSelectedGroupByFieldNames()) {
                    orderByClause.append("[" + buildEscapedName(selectedGroupByFieldName) + "]" + " " +
                            DfConfDataFieldDo.DEFAULT_SORT_ORDER_DIRECTION + ",");
                }
                orderByClause.deleteCharAt(orderByClause.length() - 1);
                sql.append(" ORDER BY " + orderByClause);
                break;
        }

        return sql.toString();
    }

    /**
     * 构造指定了"基本GROUP BY"字段的计数语句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param dimensionFieldNames
     * @param kpiFieldName
     * @param operationUserInfo
     * @return
     */
    public String buildCountSqlStatementWithBasicGroupBy(boolean processingNeeded,
                                                         String processingLogic,
                                                         String dbName,
                                                         String schemaName,
                                                         String dataObjectName,
                                                         Map<String, DfConfDataFieldDo> mapOfForm,
                                                         Map<String, DfConfDataFieldDo> mapOfTable,
                                                         List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
                                                         Map<String, String[]> generalParameterMap,
                                                         List<String> dimensionFieldNames,
                                                         String kpiFieldName,
                                                         UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        // Step 1, 1st level select

        // Step 1.1 select count
        sql.append("SELECT");
        sql.append(" COUNT(*) as cnt");
        sql.append(" FROM");

        // Step 1.2, 2nd level select start
        sql.append(" " + "(");

        // 包含可能的select从句，条件从句，group by从句，having从句
        //
        // SELECT column_name, aggregate_function(column_name)
        // FROM table_name
        // WHERE column_name operator value
        // GROUP BY column_name
        // HAVING aggregate_function(column_name) operator value
        //

        // Step 1.2.1, select clause
        sql.append(" SELECT ");
        for (String dimensionFieldName : dimensionFieldNames) {
            sql.append("[" + buildEscapedName(dimensionFieldName) + "]" + ",");
        }
        DfConfDataFieldDo kpiFieldDfConfDataFieldDo = mapOfTable.get(kpiFieldName);
        if (kpiFieldDfConfDataFieldDo != null) {
            switch (kpiFieldDfConfDataFieldDo.getFieldType()) {
                case INT:
                    sql.append("SUM(cast(" + "[" + buildEscapedName(kpiFieldName) + "]" + " as bigint)" + ")" + " AS " +
                            "[" + buildEscapedName(kpiFieldName) + "]");
                    break;
                case LONG:
                case DECIMAL:
                    sql.append("SUM([" + buildEscapedName(kpiFieldName) + "])" + " AS [" + buildEscapedName(kpiFieldName) + "]");
                    break;
                default:
                    sql.append("''" + " AS [" + buildEscapedName(kpiFieldName) + "]");
                    break;
            }
        } else {
            sql.append("''" + " AS [" + buildEscapedName(kpiFieldName) + "]");
        }

        // Step 1.2.2, from clause
        sql.append(" FROM ");
        if (processingNeeded) {
            sql.append(" " + "(" + processingLogic + ") ta");
        } else {
            sql.append(" [" + dbName + "].[" + schemaName + "].[" + dataObjectName + "]");
        }

        // Step 1.2.3, where clause
        sql.append(" WHERE 1=1 ");
        sql.append(buildWhereClause(listOfCascadeQueryParameter, generalParameterMap, mapOfForm));

        // Step 1.2.4, group by clause
        sql.append(" GROUP BY ");
        for (String dimensionFieldName : dimensionFieldNames) {
            sql.append("[" + buildEscapedName(dimensionFieldName) + "]" + ",");
        }
        sql.deleteCharAt(sql.length() - 1);

        // Step 1.3,  2nd level select end
        sql.append(") tb");

        return sql.toString();
    }

    /**
     * 构造指定了"基本GROUP BY"字段的查询语句，不含分页从句
     *
     * @param processingNeeded
     * @param processingLogic
     * @param dbName
     * @param schemaName
     * @param dataObjectName
     * @param mapOfForm
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param dimensionFieldNames
     * @param kpiFieldName
     * @param sort
     * @param operationUserInfo
     * @return
     */
    public String buildQuerySqlStatementWithBasicGroupBy(boolean processingNeeded,
                                                         String processingLogic,
                                                         String dbName,
                                                         String schemaName,
                                                         String dataObjectName,
                                                         Map<String, DfConfDataFieldDo> mapOfForm,
                                                         Map<String, DfConfDataFieldDo> mapOfTable,
                                                         List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
                                                         Map<String, String[]> generalParameterMap,
                                                         List<String> dimensionFieldNames,
                                                         String kpiFieldName,
                                                         Sort sort,
                                                         UserInfo operationUserInfo) {
        StringBuilder sql = new StringBuilder();

        // Step 1, 1st level select，处理排序条件
        sql.append("USE [" + dbName + "];");
        sql.append("SELECT ");

        for (String dimensionFieldName : dimensionFieldNames) {
            sql.append("[" + buildEscapedName(dimensionFieldName) + "]" + ",");
        }
        sql.append("[" + buildEscapedName(kpiFieldName) + "]");

        // Step 1.1, 1st level from clause
        sql.append(" FROM ");

        // Step 2, 2nd level select clause，处理查询条件
        // SELECT column_name, aggregate_function(column_name)
        // FROM table_name
        // WHERE column_name operator value
        // GROUP BY column_name
        // HAVING aggregate_function(column_name) operator value
        //

        // Step 2.1, 2nd level select start
        sql.append("(");

        // Step 2.2, select clause
        sql.append("SELECT ");
        for (String dimensionFieldName : dimensionFieldNames) {
            sql.append("[" + buildEscapedName(dimensionFieldName) + "]" + ",");
        }
        DfConfDataFieldDo kpiFieldDfConfDataFieldDo = mapOfTable.get(kpiFieldName);
        if (kpiFieldDfConfDataFieldDo != null) {
            switch (kpiFieldDfConfDataFieldDo.getFieldType()) {
                case INT:
                    sql.append("SUM(cast(" + "[" + buildEscapedName(kpiFieldName) + "]" + " as bigint)" + ")" + " AS " +
                            "[" + buildEscapedName(kpiFieldName) + "]");
                    break;
                case LONG:
                case DECIMAL:
                    sql.append("SUM([" + buildEscapedName(kpiFieldName) + "])" + " AS [" + buildEscapedName(kpiFieldName) + "]");
                    break;
                default:
                    sql.append("''" + " AS [" + buildEscapedName(kpiFieldName) + "]");
                    break;
            }
        } else {
            sql.append("''" + " AS [" + buildEscapedName(kpiFieldName) + "]");
        }

        // Step 2.3, from clause
        sql.append(" FROM ");
        if (processingNeeded) {
            sql.append(" " + "(" + processingLogic + ") ta");
        } else {
            sql.append(" [" + dbName + "].[" + schemaName + "].[" + dataObjectName + "]");
        }

        // Step 2.4, where clause
        sql.append(" WHERE 1=1 ");
        sql.append(buildWhereClause(listOfCascadeQueryParameter, generalParameterMap, mapOfForm));

        // Step 2.5, group by clause
        sql.append(" GROUP BY ");
        for (String dimensionFieldName : dimensionFieldNames) {
            sql.append("[" + buildEscapedName(dimensionFieldName) + "]" + ",");
        }
        sql.deleteCharAt(sql.length() - 1);

        // Step 2.6, 2nd level select end
        sql.append(") tb ");

        // Step 3, order by clause
        StringBuilder sortClause = new StringBuilder();
        for (String dimensionFieldName : dimensionFieldNames) {
            sortClause.append("[" + buildEscapedName(dimensionFieldName) + "]" + " ASC,");
        }
        if (sortClause.length() > 0) {
            sortClause.deleteCharAt(sortClause.length() - 1);
            sql.append(" ORDER BY ");
            sql.append(sortClause);
        }

        return sql.toString();
    }

    /**
     * 构建WHERE从句
     *
     * @param listOfCascadeQueryParameter
     * @param generalParameterMap
     * @param mapOfForm
     * @return
     */
    private String buildWhereClause(List<CascadeQueryParameterDto> listOfCascadeQueryParameter,
                                    Map<String, String[]> generalParameterMap,
                                    Map<String, DfConfDataFieldDo> mapOfForm) {
        StringBuilder sql = new StringBuilder();

        // Step 1, cascade parameter map
        if (CollectionUtils.isNotEmpty(listOfCascadeQueryParameter)) {
            // 遍历多个级联字段集合
            StringBuilder l1Condition = new StringBuilder();
            for (CascadeQueryParameterDto cascadeQueryParameterDto : listOfCascadeQueryParameter) {
                if (CollectionUtils.isNotEmpty(cascadeQueryParameterDto.getCollectionValues())) {
                    // 遍历单个级联字段集合内的多种组合
                    StringBuilder l2Condition = new StringBuilder();
                    for (Map<String, String> collectionValues : cascadeQueryParameterDto.getCollectionValues()) {
                        StringBuilder l3Condition = new StringBuilder();
                        for (Map.Entry<String, String> entry : collectionValues.entrySet()) {
                            String parameterName = entry.getKey();
                            String parameterValue = entry.getValue();
                            if (l3Condition.length() > 0) {
                                l3Condition.append(" AND " + "[" + buildEscapedName(parameterName) + "]" + " = " + "N'" + parameterValue + "'");
                            } else {
                                l3Condition.append(" " + "[" + buildEscapedName(parameterName) + "]" + " = " + "N'" + parameterValue + "'");
                            }
                        }
                        if (l3Condition.length() > 0) {
                            if (l2Condition.length() > 0) {
                                l2Condition.append(" OR " + "(" + l3Condition + ")");
                            } else {
                                l2Condition.append(" " + "(" + l3Condition + ")");
                            }
                        }
                    }
                    if (l2Condition.length() > 0) {
                        if (l1Condition.length() > 0) {
                            l1Condition.append(" AND " + "(" + l2Condition + ")");
                        } else {
                            l1Condition.append(" " + "(" + l2Condition + ")");
                        }
                    }
                }
            }

            if (l1Condition.length() > 0) {
                sql.append(" AND " + "(" + l1Condition + ")");
            }
        }

        // Step 2, general parameter map
        for (Map.Entry<String, String[]> parameter : generalParameterMap.entrySet()) {
            String parameterName = parameter.getKey();
            String[] arrayOfParameterValue = parameter.getValue();
            if (arrayOfParameterValue.length == 0) {
                continue;
            }

            DfConfDataFieldDo dfConfDataFieldDo = mapOfForm.get(parameterName);
            if (dfConfDataFieldDo == null) {
                continue;
            }

            switch (dfConfDataFieldDo.getFormElementType()) {
                case FULL_FUZZY_TEXT:
                    sql.append(buildFullFuzzyTextParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case LEFT_FUZZY_TEXT:
                    sql.append(buildLeftFuzzyTextParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case RIGHT_FUZZY_TEXT:
                    sql.append(buildRightFuzzyTextParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case DATE_RANGE:
                    sql.append(buildDateRangeParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case TIME_RANGE:
                    sql.append(buildTimeRangeParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case TIMESTAMP_RANGE:
                    sql.append(buildTimestampRangeParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case DATE_SINGLE:
                    sql.append(buildDateSingleParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case TIME_SINGLE:
                    sql.append(buildTimeSingleParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case TIMESTAMP_SINGLE:
                    sql.append(buildTimestampSingleParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                case NUMBER_RANGE:
                    sql.append(buildNumberRangeParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
                default:
                    sql.append(buildExactTextParameter(parameterName, arrayOfParameterValue, dfConfDataFieldDo));
                    break;
            }
        }

        return sql.toString();
    }

    /**
     * 构建WHERE从句中的数字范围条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildNumberRangeParameter(String parameterName, String[] arrayOfParameterValue, DfConfDataFieldDo
            dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " = " + arrayOfParameterValue[0];
        } else if (arrayOfParameterValue.length == 2) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " BETWEEN " + arrayOfParameterValue[0] +
                    " AND " + arrayOfParameterValue[1];
        } else {
            StringBuilder inClause = new StringBuilder();
            inClause.append(" AND " + "[" + buildEscapedName(parameterName) + "]" + " IN (");
            for (int i = 0; i < arrayOfParameterValue.length; i++) {
                if (i == 0) {
                    inClause.append(arrayOfParameterValue[i]);
                } else {
                    inClause.append("," + arrayOfParameterValue[i]);
                }
            }
            inClause.append(")");

            return inClause.toString();
        }
    }

    /**
     * 构建WHERE从句中的文本精确匹配条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildExactTextParameter(String parameterName, String[] arrayOfParameterValue, DfConfDataFieldDo
            dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " = " + "N'" + arrayOfParameterValue[0] + "'";
        } else {
            StringBuilder inClause = new StringBuilder();
            inClause.append(" AND " + "[" + buildEscapedName(parameterName) + "]" + " IN (");
            for (int i = 0; i < arrayOfParameterValue.length; i++) {
                if (i == 0) {
                    inClause.append("N'" + arrayOfParameterValue[i] + "'");
                } else {
                    inClause.append("," + "N'" + arrayOfParameterValue[i] + "'");
                }
            }
            inClause.append(")");

            return inClause.toString();
        }
    }

    /**
     * 构建WHERE从句中的文本全模糊匹配条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildFullFuzzyTextParameter(String parameterName, String[]
            arrayOfParameterValue, DfConfDataFieldDo dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'%" + arrayOfParameterValue[0] + "%'";
        } else {
            StringBuilder moreClause = new StringBuilder();
            moreClause.append(" AND (");
            for (int i = 0; i < arrayOfParameterValue.length; i++) {
                if (i == 0) {
                    moreClause.append("[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'%" + arrayOfParameterValue[i] + "%'");
                } else {
                    moreClause.append(" OR " + "[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'%" + arrayOfParameterValue[i] + "%'");
                }
            }
            moreClause.append(")");

            return moreClause.toString();
        }
    }

    /**
     * 构建WHERE从句中的文本左模糊匹配条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildLeftFuzzyTextParameter(String parameterName, String[]
            arrayOfParameterValue, DfConfDataFieldDo dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'%" + arrayOfParameterValue[0] + "'";
        } else {
            StringBuilder moreClause = new StringBuilder();
            moreClause.append(" AND (");
            for (int i = 0; i < arrayOfParameterValue.length; i++) {
                if (i == 0) {
                    moreClause.append("[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'%" + arrayOfParameterValue[i] + "'");
                } else {
                    moreClause.append(" OR " + "[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'%" + arrayOfParameterValue[i] + "'");
                }
            }
            moreClause.append(")");

            return moreClause.toString();
        }
    }

    /**
     * 构建WHERE从句中的文本右模糊匹配条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildRightFuzzyTextParameter(String parameterName, String[]
            arrayOfParameterValue, DfConfDataFieldDo dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'" + arrayOfParameterValue[0] + "%'";
        } else {
            StringBuilder moreClause = new StringBuilder();
            moreClause.append(" AND (");
            for (int i = 0; i < arrayOfParameterValue.length; i++) {
                if (i == 0) {
                    moreClause.append("[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'" + arrayOfParameterValue[i] + "%'");
                } else {
                    moreClause.append(" OR " + "[" + buildEscapedName(parameterName) + "]" + " LIKE " + "N'" + arrayOfParameterValue[i] + "%'");
                }
            }
            moreClause.append(")");

            return moreClause.toString();
        }
    }

    /**
     * 构建WHERE从句中的日期范围条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildDateRangeParameter(String parameterName, String[] arrayOfParameterValue, DfConfDataFieldDo
            dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 2) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " BETWEEN '" + arrayOfParameterValue[0] + " 00:00:00'" +
                    " AND '" + arrayOfParameterValue[1] + " 23:59:59'";
        }
        return "";
    }

    /**
     * 构建WHERE从句中的单个日期条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildDateSingleParameter(String parameterName, String[] arrayOfParameterValue, DfConfDataFieldDo
            dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " BETWEEN '" + arrayOfParameterValue[0] + " 00:00:00'" +
                    " AND '" + arrayOfParameterValue[0] + " 23:59:59'";
        }

        return "";
    }

    /**
     * 构建WHERE从句中的时间范围条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildTimeRangeParameter(String parameterName, String[] arrayOfParameterValue, DfConfDataFieldDo
            dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 2) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " BETWEEN '" + arrayOfParameterValue[0] +
                    "' AND '" + arrayOfParameterValue[1] + "'";
        }

        return "";
    }

    /**
     * 构建WHERE从句中的单个时间条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildTimeSingleParameter(String parameterName, String[] arrayOfParameterValue, DfConfDataFieldDo
            dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " = '" + arrayOfParameterValue[0] + "'";
        }

        return "";
    }

    /**
     * 构建WHERE从句中的时间戳范围条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildTimestampRangeParameter(String parameterName, String[]
            arrayOfParameterValue, DfConfDataFieldDo dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 2) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " BETWEEN '" + arrayOfParameterValue[0] +
                    "' AND '" + arrayOfParameterValue[1] + "'";
        }

        return "";
    }

    /**
     * 构建WHERE从句中的单个时间戳条件部分
     *
     * @param parameterName
     * @param arrayOfParameterValue
     * @param dfConfDataFieldDo
     * @return
     */
    private String buildTimestampSingleParameter(String parameterName, String[]
            arrayOfParameterValue, DfConfDataFieldDo dfConfDataFieldDo) {
        if (arrayOfParameterValue.length == 1) {
            return " AND " + "[" + buildEscapedName(parameterName) + "]" + " BETWEEN '" + arrayOfParameterValue[0] +
                    "' AND '" + arrayOfParameterValue[0] + "'";
        }

        return "";
    }

    /**
     * 构建GROUP BY启用时的SELECT Aggregation function (字段）部分
     *
     * @param dfQueryResultFieldDto
     * @return
     */
    private String buildSelectField(DfQueryResultFieldDto
                                            dfQueryResultFieldDto, Map<String, DfConfDataFieldDo> mapOfTable) {
        StringBuilder result = new StringBuilder();

        DfConfDataFieldDo dfConfDataFieldDo = mapOfTable.get(dfQueryResultFieldDto.getFieldName());

        String fieldName = buildEscapedName(dfQueryResultFieldDto.getFieldName());

        if (dfQueryResultFieldDto.getAggregateFunctionType() == null) {
            result.append(fieldName);
        } else {
            switch (dfQueryResultFieldDto.getAggregateFunctionType()) {
                case COUNT:
                    result.append("COUNT([" + fieldName + "])");
                    break;
                case SUM: {
                    if (dfConfDataFieldDo != null) {
                        switch (dfConfDataFieldDo.getFieldType()) {
                            case INT:
                                result.append("SUM(cast(" + "[" + fieldName + "]" + " as bigint)" + ")");
                                break;
                            default:
                                result.append("SUM([" + fieldName + "])");
                                break;
                        }
                    } else {
                        result.append("SUM([" + fieldName + "])");
                    }
                }
                break;
                case AVG:
                    if (dfConfDataFieldDo != null) {
                        switch (dfConfDataFieldDo.getFieldType()) {
                            case INT:
                                result.append("AVG(cast(" + "[" + fieldName + "]" + " as bigint)" + ")");
                                break;
                            default:
                                result.append("AVG([" + fieldName + "])");
                                break;
                        }
                    } else {
                        result.append("AVG([" + fieldName + "])");
                    }
                    break;
                case MAX:
                    result.append("MAX([" + fieldName + "])");
                    break;
                case MIN:
                    result.append("MIN([" + fieldName + "])");
                    break;
            }

            result.append(" AS [" + buildEscapedName(dfQueryResultFieldDto.getNewFieldLabel()) + "]");
        }

        return result.toString();
    }

    /**
     * 构建GROUP BY启用时的Having从句部分
     *
     * @param dfQueryResultFieldDto
     * @param dfQueryHavingFieldDto
     * @return
     */
    private String buildHavingCondition(DfQueryResultFieldDto dfQueryResultFieldDto,
                                        DfQueryHavingFieldDto dfQueryHavingFieldDto, Map<String,
            DfConfDataFieldDo> mapOfTable) {
        StringBuilder havingCondition = new StringBuilder();

        DfConfDataFieldDo dfConfDataFieldDo = mapOfTable.get(dfQueryResultFieldDto.getFieldName());

        String fieldName = buildEscapedName(dfQueryResultFieldDto.getFieldName());
        switch (dfQueryResultFieldDto.getAggregateFunctionType()) {
            case COUNT:
                havingCondition.append("COUNT([" + fieldName + "])");
                break;
            case SUM: {
                if (dfConfDataFieldDo != null) {
                    switch (dfConfDataFieldDo.getFieldType()) {
                        case INT:
                            havingCondition.append("SUM(cast(" + "[" + fieldName + "]" + " as bigint)" + ")");
                            break;
                        case LONG:
                        case DECIMAL:
                            havingCondition.append("SUM([" + fieldName + "])");
                            break;
                        default:
                            havingCondition.append("SUM([" + fieldName + "])");
                            break;
                    }
                } else {
                    havingCondition.append("SUM([" + fieldName + "])");
                }
            }
            break;
            case AVG:
                havingCondition.append("AVG([" + fieldName + "])");
                break;
            case MAX:
                havingCondition.append("MAX([" + fieldName + "])");
                break;
            case MIN:
                havingCondition.append("MIN([" + fieldName + "])");
                break;
        }

        switch (dfQueryHavingFieldDto.getQueryConditionType()) {
            case EQUAL_TO:
                havingCondition.append(" = " + dfQueryHavingFieldDto.getValues());
                break;
            case NOT_EQUAL_TO:
                havingCondition.append(" <> " + dfQueryHavingFieldDto.getValues());
                break;
            case GREATER_THAN:
                havingCondition.append(" > " + dfQueryHavingFieldDto.getValues());
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                havingCondition.append(" >= " + dfQueryHavingFieldDto.getValues());
                break;
            case LESS_THAN:
                havingCondition.append(" < " + dfQueryHavingFieldDto.getValues());
                break;
            case LESS_THAN_OR_EQUAL_TO:
                havingCondition.append(" <= " + dfQueryHavingFieldDto.getValues());
                break;
            case BETWEEN: {
                String[] array = dfQueryHavingFieldDto.getValues().split(",");
                havingCondition.append(" BETWEEN " + array[0] + " AND " + array[1]);
            }
            break;
            case IS_NULL:
                havingCondition.append(" IS NULL");
                break;
            case IS_NOT_NULL:
                havingCondition.append(" IS NOT NULL");
                break;
            case LIKE_LEFT_FUZZY:
                havingCondition.append(" LIKE " + "'%" + dfQueryHavingFieldDto.getValues() + "'");
                break;
            case LIKE_RIGHT_FUZZY:
                havingCondition.append(" LIKE " + "'" + dfQueryHavingFieldDto.getValues() + "%'");
                break;
            case LIKE_FULL_FUZZY:
                havingCondition.append(" LIKE " + "'%" + dfQueryHavingFieldDto.getValues() + "%'");
                break;
            case IN: {
                String[] array = dfQueryHavingFieldDto.getValues().split(",");
                havingCondition.append(" IN (");
                for (String item : array) {
                    havingCondition.append(item + ",");
                }
                havingCondition.deleteCharAt(havingCondition.length() - 1);
                havingCondition.append(")");
            }
            break;
        }

        return havingCondition.toString();
    }

    public static String buildEscapedName(String fieldName) {
        int indexOfEscapedCharacter = fieldName.indexOf(']');
        if (indexOfEscapedCharacter < 0) {
            return fieldName;
        }

        StringBuilder newFieldName = new StringBuilder();
        newFieldName.append(fieldName, 0, indexOfEscapedCharacter);
        newFieldName.append(']');
        newFieldName.append(fieldName.substring(indexOfEscapedCharacter));
        return newFieldName.toString();
    }


    private String buildClauseForAggregationTypeSum(DfConfDataFieldDo field) {
        switch (field.getFieldType()) {
            case INT:
                return "SUM(cast(" + "[" + buildEscapedName(field.getFieldName()) + "]" + " as bigint)" + ")" + " AS " +
                        "[" + buildEscapedName(field.getFieldName()) + "]";
            case LONG:
            case DECIMAL:
                return "SUM(" + "[" + buildEscapedName(field.getFieldName()) + "]" + ")" + " AS " +
                        "[" + buildEscapedName(field.getFieldName()) + "]";
            default:
                return "''" + " AS [" + buildEscapedName(field.getFieldName()) + "]";
        }
    }

    private String buildClauseForAggregationTypeAvg(DfConfDataFieldDo field) {
        switch (field.getFieldType()) {
            case INT:
                return "AVG(cast(" + "[" + buildEscapedName(field.getFieldName()) + "]" + " as bigint)" + ")" + " AS " +
                        "[" + buildEscapedName(field.getFieldName()) + "]";
            case LONG:
            case DECIMAL:
                return "AVG(" + "[" + buildEscapedName(field.getFieldName()) + "]" + ")" + " AS " +
                        "[" + buildEscapedName(field.getFieldName()) + "]";
            default:
                return "''" + " AS [" + buildEscapedName(field.getFieldName()) + "]";
        }
    }

    private String buildClauseForAggregationTypeMin(DfConfDataFieldDo field) {
        return "MIN(" + "[" + buildEscapedName(field.getFieldName()) + "]" + ")" + " AS " +
                "[" + buildEscapedName(field.getFieldName()) + "]";
    }

    private String buildClauseForAggregationTypeMax(DfConfDataFieldDo field) {
        return "MAX(" + "[" + buildEscapedName(field.getFieldName()) + "]" + ")" + " AS " +
                "[" + buildEscapedName(field.getFieldName()) + "]";
    }

    private String buildClauseForAggregationTypeCount(DfConfDataFieldDo field) {
        return "COUNT(" + "[" + buildEscapedName(field.getFieldName()) + "]" + ")" + " AS " +
                "[" + buildEscapedName(field.getFieldName()) + "]";
    }

    public static void main(String[] args) {
        String fieldName = "[玛氏地理]大区";
        System.out.println(buildEscapedName(fieldName));
    }
}
