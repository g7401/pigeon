package io.g740.pigeon.biz.object.dto.ds;

import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DsDto {
    /**
     * data source uid
     */
    private Long uid;

    /**
     * data source name
     */
    private String name;

    /**
     * data source type
     */
    private DsTypeEnum type;

    /**
     * connection profile properties
     */
    private String connectionProfileProps;
}
