package io.g740.pigeon.biz.object.dto.defaults;

import io.g740.commons.constants.AsyncJobStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author bbottong
 * <p>
 * 默认值构建流程实例
 */
@Data
public class DefaultsBuildProcessInstDto {
    /**
     * 默认值构建流程定义的UID
     */
    private Long processDefUid;

    /**
     * 默认值构建流程实例的UID
     */
    private Long uid;

    /**
     * 默认值构建流程实例执行状态
     */
    private AsyncJobStatusEnum status;

    /**
     * 流程实例执行开始时间戳
     */
    private Date startTimestamp;

    /**
     * 流程实例执行完成时间戳
     */
    private Date doneTimestamp;

    /**
     * 流程实例执行失败时间戳
     */
    private Date failedTimestamp;

    /**
     * 流程实例执行取消时间戳
     */
    private Date canceledTimestamp;

    /**
     * 流程实例执行暂停时间戳
     */
    private Date suspendedTimestamp;
}
