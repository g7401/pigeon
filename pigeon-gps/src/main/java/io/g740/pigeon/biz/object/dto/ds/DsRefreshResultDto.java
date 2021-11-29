package io.g740.pigeon.biz.object.dto.ds;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DsRefreshResultDto {
    private Long dsUid;
    private Boolean successful;
}
