package io.g740.pigeon.biz.object.dto.dictionary;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class UpdateDictionaryCategoryDto {
    private Long uid;
    private String name;
    private String description;
}
