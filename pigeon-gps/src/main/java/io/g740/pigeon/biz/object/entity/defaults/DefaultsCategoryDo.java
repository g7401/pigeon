package io.g740.pigeon.biz.object.entity.defaults;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * 默认值类目
 */
@TinyId(bizType = DefaultsCategoryDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_defaults_category")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DefaultsCategoryDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_defaults_category";

    /**
     * 为了适应前端显示，标记type
     */
    public static final String TYPE = "category";

    private Long uid;
    private String name;
    private String description;
}
