package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DfConfDto {
    private Long dfUid;
    private String dfKey;
    private DfConfBasicDto basic;
    private DfConfAdvancedDto advanced;
}
