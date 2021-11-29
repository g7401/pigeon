package io.g740.pigeon.biz.object.dto.dictionary;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class DictionaryCategoryAndStructureDto {
    private Long uid;
    private String name;
    private String description;
    private String type;
    private List<DictionaryCategoryAndStructureDto> children;
}
