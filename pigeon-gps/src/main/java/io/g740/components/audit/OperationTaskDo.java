package io.g740.components.audit;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * operation task
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_bs_operation_task")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class OperationTaskDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_bs_operation_task";

    public static final String COMMON_TASK_API_INCOMING = "api_incoming";
    public static final String COMMON_TASK_API_OUTGOING = "api_outgoing";

    /**
     * RequestIdLogFilter 分配的 requestId
     */
    @Column(name = "request_id")
    private String requestId;

    /**
     * 任务
     */
    @Column(name = "task")
    private String task;

    /**
     * 输入
     */
    @Lob
    @Column(name = "input")
    private String input;

    /**
     * 输出
     */
    @Lob
    @Column(name = "output")
    private String output;
}
