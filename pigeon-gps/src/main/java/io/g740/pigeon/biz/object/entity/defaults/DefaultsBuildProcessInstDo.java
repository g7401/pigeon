package io.g740.pigeon.biz.object.entity.defaults;

import io.g740.commons.constants.AsyncJobStatusEnum;
import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * @author bbottong
 *
 * 默认值构建流程实例
 */
@TinyId(bizType = DefaultsBuildProcessInstDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_defaults_build_process_inst")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DefaultsBuildProcessInstDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_defaults_build_process_inst";

    private Long uid;

    /**
     * 流程定义UID
     */
    @Column(name = "process_def_uid")
    private Long processDefUid;

    /**
     * 流程实例执行状态
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "status")
    private AsyncJobStatusEnum status;

    /**
     * 流程实例执行开始时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_timestamp")
    private Date startTimestamp;

    /**
     * 流程实例执行完成时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "done_timestamp")
    private Date doneTimestamp;

    /**
     * 流程实例执行失败时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "failed_timestamp")
    private Date failedTimestamp;

    /**
     * 流程实例执行取消时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "canceled_timestamp")
    private Date canceledTimestamp;

    /**
     * 流程实例执行暂停时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "suspended_timestamp")
    private Date suspendedTimestamp;
}
