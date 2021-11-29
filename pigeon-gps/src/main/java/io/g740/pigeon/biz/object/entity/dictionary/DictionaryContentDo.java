package io.g740.pigeon.biz.object.entity.dictionary;

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
 * 字典类目。字典由三个概念组成：一个或多个字典类目，每个字典类目的结构，每个字典类目的按结构组织的一个或多个键值对。
 */
@TinyId(bizType = DictionaryContentDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_dictionary_content")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DictionaryContentDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_dictionary_content";

    /**
     * 为了适应前端显示，标记type
     */
    public static final String TYPE = "content";

    private Long uid;
    private String value;
    private String label;

    /**
     * 内容树中上一级节点的ID
     */
    @Column(name = "parent_uid")
    private Long parentUid;

    /**
     * 指定字典结构的UID
     */
    @Column(name = "dictionary_structure_uid")
    private Long dictionaryStructureUid;

    /**
     * 指定字典类目的UID
     */
    @Column(name = "dictionary_category_uid")
    private Long dictionaryCategoryUid;
}
