package io.g740.commons.constants;

/**
 * @author bbottong
 */
public abstract class DatabaseConstants {
    /**
     * 所有数据表必须包括以下6个字段
     * ikste_id,
     * ikste_is_deleted,
     * ikste_create_timestamp,
     * ikste_create_by,
     * ikste_last_update_timestamp,
     * ikste_last_update_by
     */
    public static final String STANDARD_COLUMN_NAME_PREFIX = "ikste_";
    public static final String AUTO_INCREMENT_ID_DEFAULT_COLUMN_NAME = STANDARD_COLUMN_NAME_PREFIX + "id";
    public static final String DELETED_DEFAULT_COLUMN_NAME = STANDARD_COLUMN_NAME_PREFIX + "is_deleted";
    public static final String CREATE_TIMESTAMP_DEFAULT_COLUMN_NAME = STANDARD_COLUMN_NAME_PREFIX + "create_timestamp";
    public static final String CREATE_BY_DEFAULT_COLUMN_NAME = STANDARD_COLUMN_NAME_PREFIX + "create_by";
    public static final String LAST_UPDATE_TIMESTAMP_DEFAULT_COLUMN_NAME = STANDARD_COLUMN_NAME_PREFIX + "last_update_timestamp";
    public static final String LAST_UPDATE_BY_DEFAULT_COLUMN_NAME = STANDARD_COLUMN_NAME_PREFIX + "last_update_by";

    public static final Integer CHAR_DEFAULT_LENGTH = 25;
    public static final Integer VARCHAR_DEFAULT_LENGTH = 45;
    public static final Integer UUID_DEFAULT_LENGTH = 36;
    public static final Integer ID_DEFAULT_LENGTH = 20;
    public static final String DECIMAL_DEFAULT_TYPE_ATTRIBUTES = "20,4";

    public static final int DELETED_FLAG = 1;
    public static final int NOT_DELETED_FLAG = 0;

    public enum DatabaseTypeEnum {
        /**
         * MySQL
         */
        MYSQL,
        /**
         * MS SQL Server
         */
        MSSQL,
        /**
         * Oracle
         */
        ORACLE,
        /**
         * PostgreSQL
         */
        POSTGRESQL
    }

    public static String getDbUuidFunctionString(DatabaseTypeEnum databaseType) {
        switch (databaseType) {
            case MYSQL:
            case MSSQL:
                return "uuid()";
            default:
                return "";
        }
    }
}
