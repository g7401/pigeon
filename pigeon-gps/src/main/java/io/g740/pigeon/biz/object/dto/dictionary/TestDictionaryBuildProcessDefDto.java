package io.g740.pigeon.biz.object.dto.dictionary;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.object.dto.serialization.DictionaryBuildStrategyTypeEnumDeserializer;
import io.g740.pigeon.biz.share.constants.DictionaryBuildStrategyTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class TestDictionaryBuildProcessDefDto {
    /**
     * 构建策略类型
     */
    @JsonDeserialize(using = DictionaryBuildStrategyTypeEnumDeserializer.class)
    private DictionaryBuildStrategyTypeEnum buildStrategyType;

    /**
     * SQL构建策略内容
     */
    private SqlBuildStrategyContentDto sqlBuildStrategyContent;
}
