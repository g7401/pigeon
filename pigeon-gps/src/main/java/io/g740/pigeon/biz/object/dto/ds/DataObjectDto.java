package io.g740.pigeon.biz.object.dto.ds;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.serialization.DataObjectTypeEnumDeserializer;
import io.g740.pigeon.biz.share.constants.DataObjectTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DataObjectDto {
    private Long uid;

    /**
     * data source uid
     */
    private Long dsUid;

    /**
     * database name
     */
    private String dbName;

    /**
     * schema name (ONLY MSSQL CONTAINS it)
     */
    private String schemaName;

    /**
     * data object type: TABLE, VIEW
     */
    @JsonDeserialize(using = DataObjectTypeEnumDeserializer.class)
    private DataObjectTypeEnum dataObjectType;

    /**
     * data object name
     */
    private String dataObjectName;

    /**
     * data object description
     */
    private String dataObjectDescription;

}
