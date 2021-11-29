package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateOrReplaceDfConfDto {
    private Long dfUid;
    private CreateOrReplaceDfConfBasicDto basic;
    private DfConfAdvancedDto advanced;
}
