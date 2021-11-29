package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class DfConfForPageGenerationDto {
    private Long dfUid;
    private String dfKey;
    private String dfName;
    private String dfDescription;
    private List<String> dfTags;
    private List<DfConfFormFieldDto> form;
    private List<DfConfTableFieldDto> table;
    private DfConfAdvancedDto advanced;

}

