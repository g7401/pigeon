package io.g740.pigeon.biz.object.entity.df;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * DF的一个或多个标签
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_df_tag")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DfTagDo extends BaseDo {
    @Column(name = "df_uid")
    private Long dfUid;

    private String tag;
}