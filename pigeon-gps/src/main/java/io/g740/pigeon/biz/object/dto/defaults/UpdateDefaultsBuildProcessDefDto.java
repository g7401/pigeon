package io.g740.pigeon.biz.object.dto.defaults;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.object.dto.serialization.DefaultsBuildStrategyTypeEnumDeserializer;
import io.g740.pigeon.biz.object.dto.serialization.ScheduleTypeEnumJsonDeserializer;
import io.g740.pigeon.biz.share.constants.DefaultsBuildStrategyTypeEnum;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class UpdateDefaultsBuildProcessDefDto {
    private Long uid;

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
    @JsonDeserialize(using = DefaultsBuildStrategyTypeEnumDeserializer.class)
    private DefaultsBuildStrategyTypeEnum buildStrategyType;

    /**
     * SQL构建策略内容
     */
    private SqlBuildStrategyContentDto sqlBuildStrategyContent;

    /**
     * 指定默认值类目的UID
     */
    private Long defaultsCategoryUid;
}
