package io.g740.components.tag;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.TwoTuple;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * 标签
 */
@TinyId(bizType = TagDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_bs_tag")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class TagDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_bs_tag";

    private Long uid;

    private String name;

    private String description;
}
