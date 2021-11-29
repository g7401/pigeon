package io.g740.pigeon.biz.service.handler.ds.mssql;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.g740.commons.types.Handler;
import io.g740.pigeon.biz.object.dto.ds.DataFieldDto;
import io.g740.pigeon.biz.object.dto.ds.DataObjectDto;
import io.g740.pigeon.biz.object.dto.ds.IndexColumnDto;
import io.g740.pigeon.biz.object.dto.ds.IndexDto;
import io.g740.pigeon.biz.share.constants.DataFieldTypeEnum;
import io.g740.pigeon.biz.share.constants.DataObjectTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bbottong
 */
@Handler
public class MssqlConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MssqlConnector.class);

    public static final String MSSQL_JDBC_DRIVER_PREFIX = "jdbc:sqlserver://";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public Connection createConnection(String connectionProfileProps) throws Exception {
        ObjectifiedDataProps objectifiedDataProps = JSON.parseObject(connectionProfileProps, ObjectifiedDataProps.class);
        if (objectifiedDataProps == null) {
            String errorMessage = "illegal mssql connection profile props format, " + connectionProfileProps;
            throw new Exception(errorMessage);
        }

        StringBuilder connectionUrl = new StringBuilder();
        connectionUrl.append(MSSQL_JDBC_DRIVER_PREFIX);
        connectionUrl.append(objectifiedDataProps.getHost());
        connectionUrl.append(":" + objectifiedDataProps.getPort());
        if (!Strings.isNullOrEmpty(objectifiedDataProps.getMoreParameters())) {
            connectionUrl.append(";").append(objectifiedDataProps.getMoreParameters());
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionUrl.toString(),
                    objectifiedDataProps.getUser(),
                    objectifiedDataProps.getPassword());

            return connection;
        } catch (SQLException e) {
            String errorMessage =
                    "failed to create connection for " + connectionUrl + ". More info: " + e.getMessage();
            throw new Exception(errorMessage, e);
        }
    }

    /**
     * 测试连接
     *
     * @param connectionProfileProps
     * @return
     */
    public boolean testConnection(String connectionProfileProps) {
        Connection connection = null;
        try {
            connection = createConnection(connectionProfileProps);
            return true;
        } catch (Exception e) {
            LOGGER.error("test connection failed", e);
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("failed to close connection for " + connectionProfileProps, e);
                }
            }
        }
    }

    public SimpleQueryResult executeQuery(
            String connectionProfileProps,
            String sqlStatement) throws Exception {
        Connection connection = createConnection(connectionProfileProps);

        try {
            return executeQuery(connection, sqlStatement);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("failed to close connection for " + connectionProfileProps, e);
                }
            }
        }
    }

    private SimpleQueryResult executeQuery(Connection connection, String sqlStatement) throws Exception {
        SimpleQueryResult simpleQueryResult = new SimpleQueryResult();
        List<String> columnNames = new ArrayList<>();
        List<Map<String, Object>> rows = new LinkedList<>();
        simpleQueryResult.setColumnNames(columnNames);
        simpleQueryResult.setRows(rows);

        ResultSet resultSet = null;
        int columnCount = 0;

        try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            LOGGER.debug("sql={}", sqlStatement);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                // 利用这个机会收集列头（包含了列名和列序）
                if (columnNames.isEmpty()) {
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    columnCount = resultSetMetaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        // column name取column label值
                        columnNames.add(resultSetMetaData.getColumnLabel(i));
                    }
                }

                Map<String, Object> row = new HashMap<>();
                rows.add(row);
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    Object object = resultSet.getObject(columnIndex);
                    // 特殊处理MSSQL数据类型，转换成相应的Java数据类型
                    if (object instanceof Timestamp) {
                        java.util.Date dateObject = new java.util.Date(((Timestamp) object).getTime());
                        String transformedObject = new DateTime(dateObject).toString(this.dateTimeFormatter);
                        row.put(columnNames.get(columnIndex - 1), transformedObject);
                    } else if (object instanceof java.util.Date) {
                        String transformedObject = new DateTime(object).toString(this.dateTimeFormatter);
                        row.put(columnNames.get(columnIndex - 1), transformedObject);
                    } else if (object instanceof java.sql.Date) {
                        java.sql.Date sqlDate = (java.sql.Date) object;
                        java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
                        String transformedObject = new DateTime(utilDate).toString(this
                                .dateTimeFormatter);
                        row.put(columnNames.get(columnIndex - 1), transformedObject);
                    } else {
                        row.put(columnNames.get(columnIndex - 1), object);
                    }
                }
            }

            return simpleQueryResult;
        } catch (SQLException e) {
            String errorMessage = "Failed to execute sql: " + sqlStatement + ". More info: " + e.getMessage();
            throw new Exception(errorMessage, e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    String errorMessage = "Failed to close ResultSet" + ". More info: " + e.getMessage();
                    LOGGER.warn(errorMessage);
                }
            }
        }
    }

    public List<DataObjectDto> loadAllDataObjects(String connectionProfileProps) throws Exception {
        Connection connection = createConnection(connectionProfileProps);
        try {
            return executeLoadDataObjects(connection);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("failed to close connection for " + connectionProfileProps, e);
                }
            }
        }
    }

    private List<DataObjectDto> executeLoadDataObjects(Connection connection) throws Exception {
        List<DataObjectDto> listOfDataObjectDto = new LinkedList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT name FROM master.sys.databases WHERE name NOT IN ('master', 'tempdb', 'model', 'msdb')");
        SimpleQueryResult dbQueryResult = executeQuery(connection, sql.toString());
        if (CollectionUtils.isNotEmpty(dbQueryResult.getRows())) {
            for (Map<String, Object> row : dbQueryResult.getRows()) {
                String dbName = (String) row.get("name");

                try {
                    sql.setLength(0);
                    sql.append("USE " + dbName + ";");
                    sql.append("SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES");

                    SimpleQueryResult tablesQueryResult = executeQuery(connection, sql.toString());
                    if (CollectionUtils.isNotEmpty(tablesQueryResult.getRows())) {
                        for (Map<String, Object> tableRow : tablesQueryResult.getRows()) {
                            DataObjectDto dataObjectDto = new DataObjectDto();
                            dataObjectDto.setDbName(dbName);
                            dataObjectDto.setSchemaName((String) tableRow.get("TABLE_SCHEMA"));
                            dataObjectDto.setDataObjectName((String) tableRow.get("TABLE_NAME"));
                            String tableType = (String) tableRow.get("TABLE_TYPE");
                            if (tableType.equalsIgnoreCase("BASE TABLE")) {
                                dataObjectDto.setDataObjectType(DataObjectTypeEnum.TABLE);
                            } else if (tableType.equalsIgnoreCase("VIEW")) {
                                dataObjectDto.setDataObjectType(DataObjectTypeEnum.VIEW);
                            }

                            listOfDataObjectDto.add(dataObjectDto);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }

        return listOfDataObjectDto;
    }

    /**
     * 从指定data object (table or view)加载字段
     *
     * @param connectionProfileProps
     * @param dbName
     * @param schemaName
     * @param dataObjectType
     * @param dataObjectName
     * @return
     * @throws Exception
     */
    public List<DataFieldDto> loadDataFields(
            String connectionProfileProps,
            String dbName,
            String schemaName,
            DataObjectTypeEnum dataObjectType,
            String dataObjectName) throws Exception {
        Connection connection = createConnection(connectionProfileProps);

        try {
            List<DataFieldDto> listOfDataFieldDto = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("USE " + dbName + ";");
            sql.append("SELECT");
            sql.append("    COLUMN_NAME,");
            sql.append("    DATA_TYPE,");
            sql.append("    CHARACTER_MAXIMUM_LENGTH");
            sql.append("  FROM");
            sql.append("    INFORMATION_SCHEMA.COLUMNS");
            sql.append("  WHERE");
            sql.append("    TABLE_CATALOG = '" + dbName + "'");
            sql.append("    AND TABLE_SCHEMA = '" + schemaName + "'");
            sql.append("    AND TABLE_NAME = '" + dataObjectName + "'");
            sql.append("  ORDER BY");
            sql.append("    ORDINAL_POSITION ASC;");

            SimpleQueryResult queryResult = executeQuery(connection, sql.toString());
            for (Map<String, Object> row : queryResult.getRows()) {
                String fieldName = (String) row.get("COLUMN_NAME");
                String fieldTypeAndLength = (String) row.get("DATA_TYPE");
                Integer characterMaximumLength = (Integer) row.get("CHARACTER_MAXIMUM_LENGTH");

                DataFieldTypeEnum fieldType;
                Integer fieldLength = 0;
                if (fieldTypeAndLength.startsWith("bigint")) {
                    fieldType = DataFieldTypeEnum.LONG;
                } else if (fieldTypeAndLength.startsWith("int")) {
                    fieldType = DataFieldTypeEnum.INT;
                } else if (fieldTypeAndLength.startsWith("mediumint")) {
                    fieldType = DataFieldTypeEnum.INT;
                } else if (fieldTypeAndLength.startsWith("tinyint")) {
                    fieldType = DataFieldTypeEnum.INT;
                } else if (fieldTypeAndLength.startsWith("bit")) {
                    fieldType = DataFieldTypeEnum.BOOLEAN;
                } else if (fieldTypeAndLength.startsWith("datetime")) {
                    fieldType = DataFieldTypeEnum.TIMESTAMP;
                } else if (fieldTypeAndLength.startsWith("varchar")) {
                    fieldType = DataFieldTypeEnum.TEXT;
                    fieldLength = characterMaximumLength;
                } else if (fieldTypeAndLength.startsWith("nvarchar")) {
                    fieldType = DataFieldTypeEnum.TEXT;
                    fieldLength = characterMaximumLength;
                } else if (fieldTypeAndLength.startsWith("longtext")) {
                    fieldType = DataFieldTypeEnum.TEXT;
                    fieldLength = characterMaximumLength;
                } else if (fieldTypeAndLength.startsWith("decimal")) {
                    fieldType = DataFieldTypeEnum.DECIMAL;
                } else if (fieldTypeAndLength.startsWith("numeric")) {
                    fieldType = DataFieldTypeEnum.DECIMAL;
                } else if (fieldTypeAndLength.startsWith("date")) {
                    fieldType = DataFieldTypeEnum.DATE;
                } else if (fieldTypeAndLength.startsWith("time")) {
                    fieldType = DataFieldTypeEnum.TIME;
                } else {
                    fieldType = DataFieldTypeEnum.TEXT;
                    fieldLength = 45;
                }

                DataFieldDto dataFieldDto = new DataFieldDto();
                dataFieldDto.setFieldName(fieldName);
                dataFieldDto.setFieldType(fieldType);
                dataFieldDto.setFieldLength(fieldLength);
                listOfDataFieldDto.add(dataFieldDto);
            }

            return listOfDataFieldDto;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("failed to close connection for " + connectionProfileProps, e);
                }
            }
        }
    }

    /**
     * 从指定processing logic的执行结果中加载字段
     *
     * @param connectionProfileProps
     * @param processingLogic
     * @return
     * @throws Exception
     */
    public List<DataFieldDto> loadDataFields(
            String connectionProfileProps,
            String processingLogic) throws Exception {
        Connection connection = createConnection(connectionProfileProps);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 10 * FROM (");
        sql.append(processingLogic);
        sql.append(") ta");

        String sqlStatement = sql.toString();

        List<DataFieldDto> listOfDataFieldDto = new LinkedList<>();
        ResultSet resultSet = null;

        try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
            LOGGER.debug("sql={}", sqlStatement);
            resultSet = statement.executeQuery();

            if (resultSet != null) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    // column name取column label值
                    String columnName = resultSetMetaData.getColumnName(i);
                    String columnLabel = resultSetMetaData.getColumnLabel(i);
                    int columnDisplaySize = resultSetMetaData.getColumnDisplaySize(i);
                    DataFieldDto dataFieldDto = new DataFieldDto();
                    dataFieldDto.setFieldName(columnLabel);

                    DataFieldTypeEnum fieldType;
                    int columnType = resultSetMetaData.getColumnType(i);
                    if (columnType == Types.BIGINT) {
                        fieldType = DataFieldTypeEnum.LONG;
                    } else if (columnType == Types.INTEGER) {
                        fieldType = DataFieldTypeEnum.INT;
                    } else if (columnType == Types.SMALLINT) {
                        fieldType = DataFieldTypeEnum.INT;
                    } else if (columnType == Types.BIT) {
                        fieldType = DataFieldTypeEnum.BOOLEAN;
                    } else if (columnType == Types.TINYINT) {
                        fieldType = DataFieldTypeEnum.BOOLEAN;
                    } else if (columnType == Types.TIMESTAMP) {
                        fieldType = DataFieldTypeEnum.TIMESTAMP;
                    } else if (columnType == Types.VARCHAR) {
                        fieldType = DataFieldTypeEnum.TEXT;
                    } else if (columnType == Types.CHAR) {
                        fieldType = DataFieldTypeEnum.TEXT;
                    } else if (columnType == Types.LONGVARCHAR) {
                        fieldType = DataFieldTypeEnum.TEXT;
                    } else if (columnType == -9) {
                        // nvarchar
                        fieldType = DataFieldTypeEnum.TEXT;
                    } else if (columnType == Types.DECIMAL) {
                        fieldType = DataFieldTypeEnum.DECIMAL;
                    } else if (columnType == Types.NUMERIC) {
                        fieldType = DataFieldTypeEnum.DECIMAL;
                    } else if (columnType == Types.DATE) {
                        fieldType = DataFieldTypeEnum.DATE;
                    } else if (columnType == Types.TIME) {
                        fieldType = DataFieldTypeEnum.TIME;
                    } else {
                        fieldType = DataFieldTypeEnum.TEXT;
                    }

                    dataFieldDto.setFieldType(fieldType);
                    dataFieldDto.setFieldLength(columnDisplaySize);
                    dataFieldDto.setFieldDescription(columnName);
                    listOfDataFieldDto.add(dataFieldDto);
                }
            }

            return listOfDataFieldDto;
        } catch (SQLException e) {
            String errorMessage = "Failed to execute sql: " + sqlStatement + ". More info: " + e.getMessage();
            throw new Exception(errorMessage, e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    String errorMessage = "Failed to close ResultSet" + ". More info: " + e.getMessage();
                    LOGGER.warn(errorMessage);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("failed to close connection for " + connectionProfileProps, e);
                }
            }
        }
    }

    public List<IndexDto> loadIndexes(
            String connectionProfileProps,
            String dbName,
            String schemaName,
            String tableName) throws Exception {
        StringBuilder sql = new StringBuilder();

        sql.append("USE [" + dbName + "];" );
        sql.append("SELECT * FROM (");
        sql.append("    SELECT");
        sql.append("        TableName = t.Name,");
        sql.append("        IndexName = i.Name,");
        sql.append("        IndexUnique = i.is_unique,");
        sql.append("        ColumnOrdinal = Ic.key_ordinal,");
        sql.append("        ColumnName = c.name");
        sql.append("    FROM");
        sql.append("        sys.indexes i");
        sql.append("    INNER JOIN");
        sql.append("        sys.tables t ON t.object_id = i.object_id");
        sql.append("    INNER JOIN");
        sql.append("        sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id");
        sql.append("    INNER JOIN");
        sql.append("        sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id");
        sql.append("    INNER JOIN");
        sql.append("        sys.types ty ON c.system_type_id = ty.system_type_id");
        sql.append("    WHERE");
        sql.append("        t.name = '" + tableName + "'");
        sql.append(") ta");
        sql.append(" GROUP BY TableName, IndexName, IndexUnique, ColumnOrdinal, ColumnName");

        Connection connection = createConnection(connectionProfileProps);
        try {
            SimpleQueryResult queryResult = executeQuery(connection, sql.toString());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(queryResult.getRows())) {
                Map<String, IndexDto> mapOfIndex = new HashedMap();
                queryResult.getRows().forEach(row -> {
                    String indexName = (String) row.get("IndexName");
                    Boolean uniqueIndex = false;
                    Object uniqueIndexObject = row.get("IndexUnique");
                    if (uniqueIndexObject instanceof Integer) {
                        Integer uniqueIndexAsInteger = (Integer) uniqueIndexObject;
                        uniqueIndex = uniqueIndexAsInteger.equals(1);
                    } else if (uniqueIndexObject instanceof Boolean) {
                        uniqueIndex = (Boolean) uniqueIndexObject;
                    }

                    IndexDto indexDto = mapOfIndex.get(indexName);
                    if (indexDto == null) {
                        indexDto = new IndexDto();
                        indexDto.setName(indexName);
                        indexDto.setUnique(uniqueIndex);
                        indexDto.setColumns(new ArrayList<>());
                        mapOfIndex.put(indexName, indexDto);
                    }

                    Integer columnSequence = null;
                    Object columnOrdinalAsObject = row.get("ColumnOrdinal");
                    if (columnOrdinalAsObject instanceof Short) {
                        Short columnOrdinalAsShort = (Short) columnOrdinalAsObject;
                        columnSequence = columnOrdinalAsShort.intValue();
                    } else if (columnOrdinalAsObject instanceof Integer) {
                        Integer columnOrdinalAsInteger = (Integer) columnOrdinalAsObject;
                        columnSequence = columnOrdinalAsInteger;
                    } else if (columnOrdinalAsObject instanceof Long) {
                        Long columnOrdinalAsLong = (Long) columnOrdinalAsObject;
                        columnSequence = columnOrdinalAsLong.intValue();
                    }
                    String columnName = (String) row.get("ColumnName");
                    IndexColumnDto indexColumnDto = new IndexColumnDto();
                    indexColumnDto.setColumnName(columnName);
                    indexColumnDto.setSequence(columnSequence);
                    indexDto.getColumns().add(indexColumnDto);
                });

                List<IndexDto> result = mapOfIndex.values().stream().collect(Collectors.toList());

                return result;
            }

            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("failed to close connection for " + connectionProfileProps, e);
                }
            }
        }
    }

    public static class ObjectifiedDataProps {
        private String host;
        private Integer port;
        private String user;
        private String password;
        private String moreParameters;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getMoreParameters() {
            return moreParameters;
        }

        public void setMoreParameters(String moreParameters) {
            this.moreParameters = moreParameters;
        }
    }
}
