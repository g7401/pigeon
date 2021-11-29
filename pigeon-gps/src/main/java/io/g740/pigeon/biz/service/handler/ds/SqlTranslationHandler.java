package io.g740.pigeon.biz.service.handler.ds;

import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.CascadeQueryParameterDto;
import io.g740.pigeon.biz.object.dto.df.DfQueryGroupByDto;
import io.g740.pigeon.biz.object.entity.df.DfConfDataFieldDo;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import io.g740.pigeon.biz.service.handler.ds.mssql.MssqlTranslationHandler;
import io.g740.pigeon.biz.service.handler.ds.mysql.MysqlTranslationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Handler
public class SqlTranslationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlTranslationHandler.class);

    @Autowired
    private MssqlTranslationHandler mssqlTranslationHandler;

    @Autowired
    private MysqlTranslationHandler mysqlTranslationHandler;

    public String buildCountSqlStatement(DsTypeEnum dsType,
                                         boolean processingNeeded,
                                         String processingLogic,
                                         String dbName,
                                         String schemaName,
                                         String dataObjectName,
                                         Map<String, DfConfDataFieldDo> mapOfForm,
                                         List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
                                         Map<String, String[]> generalParameterMap,
                                         UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildCountSqlStatement(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildCountSqlStatement(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildQuerySqlStatement(
            DsTypeEnum dsType,
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
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            Pageable pageable,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildQuerySqlStatement(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        pageable,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildQuerySqlStatement(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        pageable,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildQuerySqlStatementWithoutPagination(
            DsTypeEnum dsType,
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
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            Sort sort,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildQuerySqlStatementWithoutPagination(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        sort,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildQuerySqlStatementWithoutPagination(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        sort,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildQuerySqlStatementWithRequiredReturnFieldNames(
            DsTypeEnum dsType,
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
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            List<String> requiredReturnFieldNames,
            Pageable pageable,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildQuerySqlStatementWithRequiredReturnFieldNames(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        requiredReturnFieldNames,
                        pageable,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildQuerySqlStatementWithRequiredReturnFieldNames(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        requiredReturnFieldNames,
                        pageable,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildQuerySqlStatementWithRequiredReturnFieldNamesWithoutPagination(
            DsTypeEnum dsType,
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
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            List<String> requiredReturnFieldNames,
            Sort sort,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildQuerySqlStatementWithRequiredReturnFieldNamesWithoutPagination(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        requiredReturnFieldNames,
                        sort,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildQuerySqlStatementWithRequiredReturnFieldNamesWithoutPagination(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder, mapOfSort, listOfSortInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        requiredReturnFieldNames,
                        sort,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildCountSqlStatementWithAdvancedGroupBy(
            DsTypeEnum dsType,
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            DfQueryGroupByDto dfQueryGroupByDto,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildCountSqlStatementWithAdvancedGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm,
                        mapOfTable,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dfQueryGroupByDto,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildCountSqlStatementWithAdvancedGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm,
                        mapOfTable,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dfQueryGroupByDto,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildQuerySqlStatementWithAdvancedGroupBy(
            DsTypeEnum dsType,
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            DfQueryGroupByDto dfQueryGroupByDto,
            Pageable pageable,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildQuerySqlStatementWithAdvancedGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dfQueryGroupByDto,
                        pageable,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildQuerySqlStatementWithAdvancedGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dfQueryGroupByDto,
                        pageable,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildQuerySqlStatementWithAdvancedGroupByWithoutPagination(
            DsTypeEnum dsType,
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<DfConfDataFieldDo> listOfTableInOrder,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            DfQueryGroupByDto dfQueryGroupByDto,
            Sort sort,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildQuerySqlStatementWithAdvancedGroupByWithoutPagination(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dfQueryGroupByDto,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildQuerySqlStatementWithAdvancedGroupByWithoutPagination(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm, mapOfTable, listOfTableInOrder,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dfQueryGroupByDto,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildCountSqlStatementWithBasicGroupBy(
            DsTypeEnum dsType,
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            List<String> dimensionFieldNames,
            String kpiFieldName,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildCountSqlStatementWithBasicGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm,
                        mapOfTable,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dimensionFieldNames,
                        kpiFieldName,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildCountSqlStatementWithBasicGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm,
                        mapOfTable,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dimensionFieldNames,
                        kpiFieldName,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String buildQuerySqlStatementWithBasicGroupByWithoutPagination(
            DsTypeEnum dsType,
            boolean processingNeeded,
            String processingLogic,
            String dbName,
            String schemaName,
            String dataObjectName,
            Map<String, DfConfDataFieldDo> mapOfForm,
            Map<String, DfConfDataFieldDo> mapOfTable,
            List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto,
            Map<String, String[]> generalParameterMap,
            List<String> dimensionFieldNames,
            String kpiFieldName,
            Sort sort,
            UserInfo operationUserInfo) throws Exception {
        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                handleEscapeCharacters(listOfCascadeQueryParameterDto);
        Map<String, String[]> newGeneralParameterMap = handleEscapeCharacters(generalParameterMap);

        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.buildQuerySqlStatementWithBasicGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, schemaName, dataObjectName,
                        mapOfForm,
                        mapOfTable,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dimensionFieldNames,
                        kpiFieldName,
                        sort,
                        operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.buildQuerySqlStatementWithBasicGroupBy(
                        processingNeeded,
                        processingLogic,
                        dbName, dataObjectName,
                        mapOfForm,
                        mapOfTable,
                        newListOfCascadeQueryParameterDto,
                        newGeneralParameterMap,
                        dimensionFieldNames,
                        kpiFieldName,
                        sort,
                        operationUserInfo);
            default:
                return null;
        }
    }

    public String addPagination(DsTypeEnum dsType,
                                String queryStatement,
                                int pageNumber,
                                int pageSize,
                                UserInfo operationUserInfo) throws Exception {
        switch (dsType) {
            case MSSQL:
                return this.mssqlTranslationHandler.addPagination(queryStatement, pageNumber, pageSize, operationUserInfo);
            case MYSQL:
                return this.mysqlTranslationHandler.addPagination(queryStatement, pageNumber, pageSize, operationUserInfo);
            default:
                return null;
        }
    }

    private Map<String, String[]> handleEscapeCharacters(Map<String, String[]> parameterMap) {
        if (parameterMap == null) {
            return null;
        }

        Map<String, String[]> newParameterMap = new HashMap<>(parameterMap.size());

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] parameterValues = entry.getValue();
            String[] newParameterValues = new String[parameterValues.length];
            for (int i = 0; i < parameterValues.length; i++) {
                String parameterValue = parameterValues[i];
                if (parameterValue.contains("'")) {
                    StringBuilder newParameterValue = new StringBuilder();
                    for (int j = 0; j < parameterValue.length(); j++) {
                        String character = parameterValue.substring(j, j + 1);
                        if (character.equals("'")) {
                            newParameterValue.append("''");
                        } else {
                            newParameterValue.append(character);
                        }
                    }
                    newParameterValues[i] = newParameterValue.toString();
                } else {
                    newParameterValues[i] = parameterValue;
                }
            }
            newParameterMap.put(entry.getKey(), newParameterValues);
        }

        return newParameterMap;
    }

    private List<CascadeQueryParameterDto> handleEscapeCharacters(List<CascadeQueryParameterDto> listOfCascadeQueryParameterDto) {
        if (listOfCascadeQueryParameterDto == null) {
            return null;
        }

        List<CascadeQueryParameterDto> newListOfCascadeQueryParameterDto =
                new ArrayList<>(listOfCascadeQueryParameterDto.size());

        for (CascadeQueryParameterDto cascadeQueryParameterDto : listOfCascadeQueryParameterDto) {
            CascadeQueryParameterDto newCascadeQueryParameterDto = new CascadeQueryParameterDto();
            newListOfCascadeQueryParameterDto.add(newCascadeQueryParameterDto);

            List<Map<String, String>> newCollectionValues =
                    new ArrayList<>(cascadeQueryParameterDto.getCollectionValues().size());
            newCascadeQueryParameterDto.setCollectionValues(newCollectionValues);
            newCascadeQueryParameterDto.setCascadeCollectionName(cascadeQueryParameterDto.getCascadeCollectionName());

            List<Map<String, String>> collectionValues = cascadeQueryParameterDto.getCollectionValues();
            for (Map<String, String> parameterMap : collectionValues) {
                Map<String, String> newParameterMap = new HashMap<>();
                newCollectionValues.add(newParameterMap);

                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    String parameterName = entry.getKey();
                    String parameterValue = entry.getValue();
                    if (parameterValue.contains("'")) {
                        StringBuilder newParameterValue = new StringBuilder();
                        for (int j = 0; j < parameterValue.length(); j++) {
                            String character = parameterValue.substring(j, j + 1);
                            if (character.equals("'")) {
                                newParameterValue.append("''");
                            } else {
                                newParameterValue.append(character);
                            }
                        }
                        newParameterMap.put(parameterName, newParameterValue.toString());
                    } else {
                        newParameterMap.put(parameterName, parameterValue);
                    }
                }
            }
        }

        return newListOfCascadeQueryParameterDto;
    }
}
