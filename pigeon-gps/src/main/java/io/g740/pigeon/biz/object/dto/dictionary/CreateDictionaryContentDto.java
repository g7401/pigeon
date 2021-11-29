package io.g740.pigeon.biz.object.dto.dictionary;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateDictionaryContentDto {
    private String value;
    private String label;
    /**
     * 结构中上一级节点的UID
     */
    private Long parentUid;
    /**
     * 指定字典类目的UID
     */
    private Long dictionaryCategoryUid;
}
