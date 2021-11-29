package io.g740.pigeon.biz.object.entity.dictionary;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import io.g740.pigeon.biz.share.constants.DictionaryBuildStrategyTypeEnum;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * 字典构建流程定义
 */
@TinyId(bizType = DictionaryBuildProcessDefDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_dictionary_build_process_def")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DictionaryBuildProcessDefDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_dictionary_build_process_def";

    private Long uid;

    /**
     * 是否启用该流程
     */
    @Column(name = "enabled", columnDefinition = "boolean default false")
    private Boolean enabled;

    /**
     * 流程调度类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "schedule_type")
    private ScheduleTypeEnum scheduleType;

    /**
     * 流程调度明细
     */
    @Column(name = "schedule_type_ext_details")
    private String scheduleTypeExtDetails;

    /**
     * 构建策略类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "build_strategy_type")
    private DictionaryBuildStrategyTypeEnum buildStrategyType;

    /**
     * 构建策略内容
     */
    @Column(name = "build_strategy_content", length = 3096)
    private String buildStrategyContent;

    /**
     * 指定字典类目的UID
     */
    @Column(name = "dictionary_category_uid")
    private Long dictionaryCategoryUid;
}
