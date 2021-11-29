package io.g740.pigeon.biz.object.dto.dictionary;

import lombok.Data;

/**
 * @author bbottong
 *
 * 为指定字典类目创建结构
 */
@Data
public class CreateDictionaryStructureDto {
    private String name;
    private String description;
    /**
     * 结构中上一级节点的UID
     */
    private Long parentUid;
    /**
     * 指定字典类目的UID
     */
    private Long dictionaryCategoryUid;
}
