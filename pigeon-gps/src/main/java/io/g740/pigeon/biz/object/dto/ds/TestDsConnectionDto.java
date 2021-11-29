package io.g740.pigeon.biz.object.dto.ds;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.serialization.DsTypeEnumDeserializer;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 *
 * 测试数据源连接
 */
@Data
public class TestDsConnectionDto {
    /**
     * data source type
     */
    @JsonDeserialize(using = DsTypeEnumDeserializer.class)
    private DsTypeEnum dsType;
    /**
     * connection profile properties
     */
    private String connectionProfileProps;
}
