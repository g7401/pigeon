package io.g740.pigeon.biz.service.handler.ds.mysql;

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
public class MysqlConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlConnector.class);

    public static final String MYSQL_JDBC_DRIVER_PREFIX = "jdbc:mysql://";
    public static final String USER_SYMBOL = "user";
    public static final String PASSWORD_SYMBOL = "password";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public Connection createConnection(String connectionProfileProps, String dynamicParameters) throws Exception {
        ObjectifiedDataProps objectifiedDataProps = JSON.parseObject(connectionProfileProps,
                ObjectifiedDataProps.class);
        if (objectifiedDataProps == null) {
            String errorMessage = "illegal mssql connection profile props format, " + connectionProfileProps;
            throw new Exception(errorMessage);
        }

        StringBuilder connectionUrl = new StringBuilder();
        connectionUrl.append(MYSQL_JDBC_DRIVER_PREFIX);
        connectionUrl.append(objectifiedDataProps.getHost());
        connectionUrl.append(":" + objectifiedDataProps.getPort());
        if (!Strings.isNullOrEmpty(objectifiedDataProps.getMoreParameters())) {
            connectionUrl.append("?").append(objectifiedDataProps.getMoreParameters());

            if (!Strings.isNullOrEmpty(dynamicParameters)) {
                connectionUrl.append("&").append(dynamicParameters);
            }
        } else {
            if (!Strings.isNullOrEmpty(dynamicParameters)) {
                connectionUrl.append("?").append(dynamicParameters);
            }
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

    public Connection createConnection(String connectionProfileProps) throws Exception {
        ObjectifiedDataProps objectifiedDataProps = JSON.parseObject(connectionProfileProps,
                ObjectifiedDataProps.class);
        if (objectifiedDataProps == null) {
            String errorMessage = "illegal mssql connection profile props format, " + connectionProfileProps;
            throw new Exception(errorMessage);
        }

        StringBuilder connectionUrl = new StringBuilder();
        connectionUrl.append(MYSQL_JDBC_DRIVER_PREFIX);
        connectionUrl.append(objectifiedDataProps.getHost());
        connectionUrl.append(":" + objectifiedDataProps.getPort());
        if (!Strings.isNullOrEmpty(objectifiedDataProps.getMoreParameters())) {
            connectionUrl.append("?").append(objectifiedDataProps.getMoreParameters());
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
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_COMMENT, TABLE_TYPE FROM information_schema.tables WHERE " +
                "TABLE_SCHEMA NOT IN \n" +
                "('information_schema', 'mysql', 'performance_schema', 'sys');");

        SimpleQueryResult queryResult = executeQuery(connection, sql.toString());

        return transform(queryResult.getColumnNames(), queryResult.getRows());
    }

    private List<DataObjectDto> transform(
            List<String> columnNames,
            List<Map<String, Object>> rows) throws Exception {
        List<DataObjectDto> dataObjectDtoList = new LinkedList<>();
        for (Map<String, Object> row : rows) {
            // 取database name
            String tableSchema = String.valueOf(row.get("TABLE_SCHEMA"));
            String tableName = String.valueOf(row.get("TABLE_NAME"));
            String tableComment = String.valueOf(row.get("TABLE_COMMENT"));
            String tableType = String.valueOf(row.get("TABLE_TYPE"));

            DataObjectDto dataObjectDto = new DataObjectDto();
            dataObjectDto.setDbName(tableSchema);
            dataObjectDto.setDataObjectName(tableName);
            dataObjectDto.setDataObjectDescription(tableComment);
            if ("BASE TABLE".equalsIgnoreCase(tableType)) {
                dataObjectDto.setDataObjectType(DataObjectTypeEnum.TABLE);
            } else if ("VIEW".equalsIgnoreCase(tableType)) {
                dataObjectDto.setDataObjectType(DataObjectTypeEnum.VIEW);
            }

            dataObjectDtoList.add(dataObjectDto);
        }

        return dataObjectDtoList;
    }

    /**
     * 从指定data object (table or view)加载字段
     *
     * @param connectionProfileProps
     * @param dbName
     * @param dataObjectType
     * @param dataObjectName
     * @return
     * @throws Exception
     */
    public List<DataFieldDto> loadDataFields(
            String connectionProfileProps,
            String dbName,
            DataObjectTypeEnum dataObjectType,
            String dataObjectName) throws Exception {
        Connection connection = createConnection(connectionProfileProps);

        try {
            List<DataFieldDto> listOfDataFieldDto = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SHOW FULL COLUMNS FROM");
            sql.append(" " + dbName + "." + dataObjectName);
            SimpleQueryResult queryResult = executeQuery(connection, sql.toString());
            for (Map<String, Object> row : queryResult.getRows()) {
                String fieldName = (String) row.get("Field");
                String fieldTypeAndLength = (String) row.get("Type");
                String fieldDescription = (String) row.get("Comment");

                DataFieldTypeEnum fieldType;
                Integer fieldLength = 0;
                if (fieldTypeAndLength.startsWith("bigint")) {
                    fieldType = DataFieldTypeEnum.LONG;
                } else if (fieldTypeAndLength.startsWith("int")) {
                    fieldType = DataFieldTypeEnum.INT;
                } else if (fieldTypeAndLength.startsWith("mediumint")) {
                    fieldType = DataFieldTypeEnum.INT;
                } else if (fieldTypeAndLength.startsWith("smallint")) {
                    fieldType = DataFieldTypeEnum.INT;
                } else if (fieldTypeAndLength.startsWith("tinyint")) {
                    fieldType = DataFieldTypeEnum.BOOLEAN;
                } else if (fieldTypeAndLength.startsWith("datetime")) {
                    fieldType = DataFieldTypeEnum.TIMESTAMP;
                } else if (fieldTypeAndLength.startsWith("varchar")) {
                    fieldType = DataFieldTypeEnum.TEXT;
                    fieldLength = Integer.parseInt(fieldTypeAndLength.substring("varchar".length() + 1,
                            fieldTypeAndLength.length() - 1));
                } else if (fieldTypeAndLength.startsWith("char")) {
                    fieldType = DataFieldTypeEnum.TEXT;
                    fieldLength = Integer.parseInt(fieldTypeAndLength.substring("char".length() + 1,
                            fieldTypeAndLength.length() - 1));
                } else if (fieldTypeAndLength.startsWith("longtext")) {
                    fieldType = DataFieldTypeEnum.TEXT;
                    fieldLength = 1024;
                } else if (fieldTypeAndLength.startsWith("decimal")) {
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
                dataFieldDto.setFieldDescription(fieldDescription);
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
        sql.append("SELECT * FROM (");
        sql.append(processingLogic);
        sql.append(") ta");

        // pagination clause
        sql.append(" LIMIT " + 0 + "," + 10);

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
                    DataFieldDto dataFieldDto = new DataFieldDto();
                    dataFieldDto.setFieldName(resultSetMetaData.getColumnLabel(i));

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
                    } else if (columnType == Types.DECIMAL) {
                        fieldType = DataFieldTypeEnum.DECIMAL;
                    } else if (columnType == Types.DATE) {
                        fieldType = DataFieldTypeEnum.DATE;
                    } else if (columnType == Types.TIME) {
                        fieldType = DataFieldTypeEnum.TIME;
                    } else {
                        fieldType = DataFieldTypeEnum.TEXT;
                    }

                    dataFieldDto.setFieldType(fieldType);
                    dataFieldDto.setFieldLength(resultSetMetaData.getColumnDisplaySize(i));
                    dataFieldDto.setFieldDescription(resultSetMetaData.getColumnName(i));
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

    public List<IndexDto> loadIndexes(String connectionProfileProps, String dbName, String tableName) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SHOW INDEX FROM " + "`" + dbName + "`" + "." + "`" + tableName + "`");

        Connection connection = createConnection(connectionProfileProps);
        try {
            SimpleQueryResult queryResult = executeQuery(connection, sql.toString());
            if (CollectionUtils.isNotEmpty(queryResult.getRows())) {
                Map<String, IndexDto> mapOfIndex = new HashedMap();
                queryResult.getRows().forEach(row -> {
                    String indexName = (String) row.get("Key_name");
                    Boolean uniqueIndex = false;
                    Object uniqueIndexObject = row.get("Non_unique");
                    if (uniqueIndexObject instanceof Short) {
                        Short uniqueIndexAsShort = (Short) uniqueIndexObject;
                        uniqueIndex = uniqueIndexAsShort.equals(1);
                    } else if (uniqueIndexObject instanceof Integer) {
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
                    Object columnSequenceAsObject = row.get("Seq_in_index");
                    if (columnSequenceAsObject instanceof Short) {
                        columnSequence = ((Short) columnSequenceAsObject).intValue();
                    } else if (columnSequenceAsObject instanceof Integer) {
                        columnSequence = (Integer) columnSequenceAsObject;
                    } else if (columnSequenceAsObject instanceof Long) {
                        columnSequence = ((Long) columnSequenceAsObject).intValue();
                    }

                    String columnName = (String) row.get("Column_name");
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
}
