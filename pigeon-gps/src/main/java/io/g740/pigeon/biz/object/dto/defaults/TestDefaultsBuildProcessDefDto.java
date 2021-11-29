package io.g740.pigeon.biz.object.dto.defaults;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.object.dto.serialization.DefaultsBuildStrategyTypeEnumDeserializer;
import io.g740.pigeon.biz.share.constants.DefaultsBuildStrategyTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class TestDefaultsBuildProcessDefDto {
    /**
     * 构建策略类型
     */
    @JsonDeserialize(using = DefaultsBuildStrategyTypeEnumDeserializer.class)
    private DefaultsBuildStrategyTypeEnum buildStrategyType;

    /**
     * SQL构建策略内容
     */
    private SqlBuildStrategyContentDto sqlBuildStrategyContent;
}
