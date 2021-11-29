package io.g740.pigeon.biz.object.entity.ds;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import io.g740.pigeon.biz.share.constants.DataObjectTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * Data Object，数据对象
 */
@TinyId(bizType = DsDataObjectDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_ds_data_object")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DsDataObjectDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_ds_data_object";

    private Long uid;

    /**
     * data source uid
     */
    @Column(name = "ds_uid")
    private Long dsUid;

    /**
     * database name
     */
    @Column(name = "db_name")
    private String dbName;

    /**
     * schema name (ONLY MSSQL CONTAINS it)
     */
    @Column(name = "schema_name")
    private String schemaName;

    /**
     * data object type: TABLE, VIEW
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "data_object_type")
    private DataObjectTypeEnum dataObjectType;

    /**
     * data object name
     */
    @Column(name = "data_object_name")
    private String dataObjectName;

    /**
     * data object description
     */
    @Column(name = "data_object_description")
    private String dataObjectDescription;
}
