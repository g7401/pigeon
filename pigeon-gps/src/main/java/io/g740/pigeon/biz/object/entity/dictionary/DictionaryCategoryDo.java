package io.g740.pigeon.biz.object.entity.dictionary;

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
 * 字典类目。字典由三个概念组成：一个或多个字典类目，每个字典类目的结构，每个字典类目的按结构组织的一个或多个键值对。
 */
@TinyId(bizType = DictionaryCategoryDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_dictionary_category")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DictionaryCategoryDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_dictionary_category";

    /**
     * 为了适应前端显示，标记type
     */
    public static final String TYPE = "category";

    private Long uid;
    private String name;
    private String description;
}
