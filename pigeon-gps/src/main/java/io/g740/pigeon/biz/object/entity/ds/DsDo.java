package io.g740.pigeon.biz.object.entity.ds;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * Data Source, DS, 数据源
 */
@TinyId(bizType = DsDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_ds")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DsDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_ds";

    private Long uid;
    private String name;
    private String description;

    @Enumerated(value = EnumType.ORDINAL)
    private DsTypeEnum type;

    @Column(name = "connection_profile_props", length = 1024)
    private String connectionProfileProps;
}
