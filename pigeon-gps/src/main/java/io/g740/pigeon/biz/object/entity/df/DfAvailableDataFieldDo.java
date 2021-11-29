package io.g740.pigeon.biz.object.entity.df;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.pigeon.biz.share.constants.DataFieldTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * DF的可用字段
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_df_available_data_field")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DfAvailableDataFieldDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_df_available_data_field";

    @Column(name = "df_uid")
    private Long dfUid;

    /**
     * 字段名称
     */
    @Column(name = "field_name")
    private String fieldName;

    /**
     * 字段类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "field_type")
    private DataFieldTypeEnum fieldType;

    /**
     * 字段长度，只有TEXT类型需要记录字段长度
     */
    @Column(name = "field_length")
    private Integer fieldLength;

    /**
     * 字段描述
     */
    @Column(name = "field_description")
    private String fieldDescription;

    /**
     * 字段在所有字段中的序号（从0开始计数）
     */
    @Column(name = "ordinal_position")
    private Integer ordinalPosition;
}