package io.g740.pigeon.biz.object.dto.dictionary;

import lombok.Data;

/**
 * @author bbottong
 *
 */
@Data
public class UpdateDictionaryContentDto {
    private Long uid;
    private String value;
    private String label;
}
