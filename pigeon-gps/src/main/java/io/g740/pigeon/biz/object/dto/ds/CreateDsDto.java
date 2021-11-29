package io.g740.pigeon.biz.object.dto.ds;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.serialization.DsTypeEnumDeserializer;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateDsDto {
    /**
     * data source name
     */
    private String name;

    /**
     * data source type
     */
    @JsonDeserialize(using = DsTypeEnumDeserializer.class)
    private DsTypeEnum type;

    /**
     * connection profile properties
     */
    private String connectionProfileProps;
}
