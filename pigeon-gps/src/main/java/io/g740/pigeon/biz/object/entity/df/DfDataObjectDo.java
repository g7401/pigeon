package io.g740.pigeon.biz.object.entity.df;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.pigeon.biz.share.constants.DataObjectDependencyTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * DF所依存的Primary(主要的) Data Object，以及可能存在的Secondary(次要的) Data Object(s)
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_df_data_object")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DfDataObjectDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_df_data_object";

    @Column(name = "df_uid")
    private Long dfUid;

    /**
     * DF所依存的Data Object，是主要的，还是次要的
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "data_object_dependency_type")
    private DataObjectDependencyTypeEnum dataObjectDependencyType;

    /**
     * data object uid
     */
    @Column(name = "data_object_uid")
    private Long dataObjectUid;
}