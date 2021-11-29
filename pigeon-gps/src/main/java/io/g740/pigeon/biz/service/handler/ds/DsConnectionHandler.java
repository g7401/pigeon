package io.g740.pigeon.biz.service.handler.ds;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.pigeon.biz.object.dto.ds.DataFieldDto;
import io.g740.pigeon.biz.object.dto.ds.DataObjectDto;
import io.g740.pigeon.biz.object.dto.ds.IndexDto;
import io.g740.pigeon.biz.object.dto.ds.TestDsConnectionDto;
import io.g740.pigeon.biz.service.handler.ds.mssql.MssqlConnector;
import io.g740.pigeon.biz.service.handler.ds.mysql.MysqlConnector;
import io.g740.pigeon.biz.share.constants.DataObjectTypeEnum;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Handler
public class DsConnectionHandler {

    @Autowired
    private MssqlConnector mssqlConnector;

    @Autowired
    private MysqlConnector mysqlConnector;

    public Boolean testDsConnection(TestDsConnectionDto testDsConnectionDto) {
        switch (testDsConnectionDto.getDsType()) {
            case MSSQL:
                return this.mssqlConnector.testConnection(testDsConnectionDto.getConnectionProfileProps());
            case MYSQL:
                return this.mysqlConnector.testConnection(testDsConnectionDto.getConnectionProfileProps());
            default:
                return false;
        }
    }

    public SimpleQueryResult testQuery(DsTypeEnum dsType,
                                       String dsConnectionProfileProps,
                                       String queryStatement, int offset, int size) throws Exception {
        switch (dsType) {
            case MSSQL: {
                StringBuilder newQueryStatement = new StringBuilder();
                newQueryStatement.append("SELECT TOP " + size + " * FROM (");
                newQueryStatement.append(queryStatement);
                newQueryStatement.append(") xxa");
                return this.mssqlConnector.executeQuery(dsConnectionProfileProps, newQueryStatement.toString());
            }
            case MYSQL: {
                StringBuilder newQueryStatement = new StringBuilder();
                newQueryStatement.append("SELECT * FROM (");
                newQueryStatement.append(queryStatement);
                newQueryStatement.append(") xxa");
                newQueryStatement.append(" LIMIT " + offset + "," + size);
                return this.mysqlConnector.executeQuery(dsConnectionProfileProps, newQueryStatement.toString());
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }
    }

    /**
     * 在指定数据源中执行查询语句
     *
     * @param dsType                   数据源类型
     * @param dsConnectionProfileProps 数据源连接参数
     * @param queryStatement           查询语句
     * @return
     * @throws Exception
     */
    public SimpleQueryResult executeQuery(DsTypeEnum dsType,
                                          String dsConnectionProfileProps,
                                          String queryStatement) throws Exception {
        switch (dsType) {
            case MSSQL:
                return this.mssqlConnector.executeQuery(dsConnectionProfileProps, queryStatement);
            case MYSQL:
                return this.mysqlConnector.executeQuery(dsConnectionProfileProps, queryStatement);
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }
    }

    /**
     * 列出指定数据源所有的 database, schema, table or view
     *
     * @param dsType
     * @param dsConnectionProfileProps
     * @return
     * @throws ServiceException
     */
    public List<DataObjectDto> loadAllDataObjects(DsTypeEnum dsType,
                                                  String dsConnectionProfileProps) throws Exception {
        switch (dsType) {
            case MSSQL: {
                return this.mssqlConnector.loadAllDataObjects(dsConnectionProfileProps);
            }
            case MYSQL: {
                return this.mysqlConnector.loadAllDataObjects(dsConnectionProfileProps);
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }
    }

    public List<DataFieldDto> loadDataFields(DsTypeEnum dsType,
                                             String dsConnectionProfileProps,
                                             String dbName,
                                             String schemaName,
                                             DataObjectTypeEnum dataObjectType,
                                             String dataObjectName) throws Exception {
        switch (dsType) {
            case MSSQL: {
                return this.mssqlConnector.loadDataFields(dsConnectionProfileProps,
                        dbName, schemaName, dataObjectType, dataObjectName);
            }
            case MYSQL: {
                return this.mysqlConnector.loadDataFields(dsConnectionProfileProps,
                        dbName, dataObjectType, dataObjectName);
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }
    }

    public List<DataFieldDto> loadDataFields(DsTypeEnum dsType,
                                             String dsConnectionProfileProps,
                                             String processingLogic) throws Exception {
        switch (dsType) {
            case MSSQL: {
                return this.mssqlConnector.loadDataFields(dsConnectionProfileProps,
                        processingLogic);
            }
            case MYSQL: {
                return this.mysqlConnector.loadDataFields(dsConnectionProfileProps,
                        processingLogic);
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }
    }

    public List<IndexDto> loadIndexes(DsTypeEnum dsType,
                                      String dsConnectionProfileProps,
                                      String dbName,
                                      String schemaName,
                                      String tableName) throws Exception {
        switch (dsType) {
            case MSSQL: {
                return this.mssqlConnector.loadIndexes(dsConnectionProfileProps, dbName, schemaName, tableName);
            }
            case MYSQL: {
                return this.mysqlConnector.loadIndexes(dsConnectionProfileProps, dbName, tableName);
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }
    }

    public PageResult<Map<String, Object>> executePageQuery(DsTypeEnum dsType,
                                                            String dsConnectionProfileProps,
                                                            String countStatement,
                                                            String queryStatement,
                                                            Pageable pageable) throws Exception {
        PageResult<Map<String, Object>> result = new PageResult<>();
        SimpleQueryResult countResult;
        SimpleQueryResult queryResult;

        switch (dsType) {
            case MSSQL: {
                countResult = this.mssqlConnector.executeQuery(dsConnectionProfileProps,
                        countStatement);
                break;
            }
            case MYSQL: {
                countResult = this.mysqlConnector.executeQuery(dsConnectionProfileProps,
                        countStatement);
                break;
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }

        if (countResult == null ||
                CollectionUtils.isEmpty(countResult.getColumnNames()) ||
                CollectionUtils.isEmpty(countResult.getRows())) {
            result.setTotalPages(0);
            result.setTotalElements(0L);
            result.setPageSize(pageable.getPageSize());
            result.setPageNumber(pageable.getPageNumber());
            result.setNumberOfElements(0);

            return result;
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
            result.setTotalPages(0);
            result.setTotalElements(0L);
            result.setPageSize(pageable.getPageSize());
            result.setPageNumber(pageable.getPageNumber());
            result.setNumberOfElements(0);

            return result;
        }

        switch (dsType) {
            case MSSQL: {
                queryResult = this.mssqlConnector.executeQuery(dsConnectionProfileProps,
                        queryStatement);
                break;
            }
            case MYSQL: {
                queryResult = this.mysqlConnector.executeQuery(dsConnectionProfileProps,
                        queryStatement);
                break;
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }

        if (queryResult != null && CollectionUtils.isNotEmpty(queryResult.getRows())) {
            result.setNumberOfElements(queryResult.getRows().size());
            result.setPageNumber(pageable.getPageNumber());
            result.setPageSize(pageable.getPageSize());
            result.setTotalElements(count.longValue());
            Long totalPages = (count / pageable.getPageSize());
            if (count % pageable.getPageSize() != 0) {
                result.setTotalPages(totalPages.intValue() + 1);
            } else {
                result.setTotalPages(totalPages.intValue());
            }
            result.setContent(queryResult.getRows());
        } else {
            Long totalPages = count / pageable.getPageSize();
            if (count % pageable.getPageSize() != 0) {
                result.setTotalPages(totalPages.intValue() + 1);
            } else {
                result.setTotalPages(totalPages.intValue());
            }
            result.setTotalElements(count.longValue());
            result.setPageSize(pageable.getPageSize());
            result.setPageNumber(pageable.getPageNumber());
            result.setNumberOfElements(0);
        }

        return result;
    }

    public PageResult<Map<String, Object>> executePageQuery(DsTypeEnum dsType,
                                                            String dsConnectionProfileProps,
                                                            String queryStatement,
                                                            Pageable pageable) throws Exception {
        PageResult<Map<String, Object>> result = new PageResult<>();
        SimpleQueryResult queryResult;

        switch (dsType) {
            case MSSQL: {
                queryResult = this.mssqlConnector.executeQuery(dsConnectionProfileProps,
                        queryStatement);
                break;
            }
            case MYSQL: {
                queryResult = this.mysqlConnector.executeQuery(dsConnectionProfileProps,
                        queryStatement);
                break;
            }
            default:
                throw new Exception("unsupported ds type, " + dsType);
        }

        if (queryResult != null && CollectionUtils.isNotEmpty(queryResult.getRows())) {
            result.setNumberOfElements(queryResult.getRows().size());
            result.setPageNumber(pageable.getPageNumber());
            result.setPageSize(pageable.getPageSize());
            result.setContent(queryResult.getRows());
        } else {
            result.setPageSize(pageable.getPageSize());
            result.setPageNumber(pageable.getPageNumber());
            result.setNumberOfElements(0);
        }

        return result;
    }
}
