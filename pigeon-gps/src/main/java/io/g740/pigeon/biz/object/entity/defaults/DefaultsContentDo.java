package io.g740.pigeon.biz.object.entity.defaults;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * 默认值内容
 */
@TinyId(bizType = DefaultsContentDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_defaults_content")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DefaultsContentDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_defaults_content";

    /**
     * 为了适应前端显示，标记type
     */
    public static final String TYPE = "content";

    private Long uid;
    private String value;
    private String label;

    /**
     * 指定默认值类目的UID
     */
    @Column(name = "defaults_category_uid")
    private Long defaultsCategoryUid;
}
