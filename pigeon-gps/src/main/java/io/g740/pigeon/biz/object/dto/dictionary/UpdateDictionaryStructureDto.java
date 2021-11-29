package io.g740.pigeon.biz.object.dto.dictionary;

import lombok.Data;

/**
 * @author bbottong
 *
 * 为指定字典类目创建结构
 */
@Data
public class UpdateDictionaryStructureDto {
    private Long uid;
    private String name;
    private String description;
}
