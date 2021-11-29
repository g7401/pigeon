package io.g740.pigeon.biz.object.dto.dictionary;

import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.share.constants.DictionaryBuildStrategyTypeEnum;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 *
 * 字典构建流程定义
 */
@Data
public class DictionaryBuildProcessDefDto {
    /**
     * 字典构建流程定义的UID
     */
    private Long uid;

    /**
     * 是否启用该流程
     */
    private Boolean enabled;

    /**
     * 流程调度类型
     */
    private ScheduleTypeEnum scheduleType;

    /**
     * 流程调度明细
     */
    private String scheduleTypeExtDetails;

    /**
     * 构建策略类型
     */
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
