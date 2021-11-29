package io.g740.pigeon.biz.object.dto.dictionary;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.object.dto.serialization.DictionaryBuildStrategyTypeEnumDeserializer;
import io.g740.pigeon.biz.object.dto.serialization.ScheduleTypeEnumJsonDeserializer;
import io.g740.pigeon.biz.share.constants.DictionaryBuildStrategyTypeEnum;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateDictionaryBuildProcessDefDto {
    /**
     * 是否启用该流程
     */
    private Boolean enabled;

    /**
     * 流程调度类型
     */
    @JsonDeserialize(using = ScheduleTypeEnumJsonDeserializer.class)
    private ScheduleTypeEnum scheduleType;

    /**
     * 流程调度明细
     */
    private String scheduleTypeExtDetails;

    /**
     * 构建策略类型
     */
    @JsonDeserialize(using = DictionaryBuildStrategyTypeEnumDeserializer.class)
    private DictionaryBuildStrategyTypeEnum buildStrategyType;

    /**
     * SQL构建策略内容
     */
    private SqlBuildStrategyContentDto sqlBuildStrategyContent;

    /**
     * 指定字典类目的UID
     */
    private Long dictionaryCategoryUid;
}
