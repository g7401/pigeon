package io.g740.pigeon.biz.object.entity.export;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Async Export Job Conf / 异步导出工作配置
 *
 * @author bbottong
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_bs_async_export_job_conf")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class AsyncExportJobConfDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_bs_async_export_job_conf";

    /**
     * 正在等待的任务，最大数量
     */
    @Column(name = "max_waiting_threshold", columnDefinition = "int default 5 ")
    private Integer maxWaitingThreshold;

    /**
     * 正在运行的任务，最大数量
     */
    @Column(name = "max_running_threshold", columnDefinition = "int default 5")
    private Integer maxRunningThreshold;

    /**
     * 分页大小，取数按分页方式
     */
    @Column(name = "page_size", columnDefinition = "int default 30000")
    private Integer pageSize;

    /**
     * Excel导出的一个分页需要按多少条数据写一次文件
     */
    @Column(name = "excel_segment_size", columnDefinition = "int default 5000")
    private Integer excelSegmentSize;

    /**
     * 所有任务记录保存天数，最大天数
     */
    @Column(name = "reserve_days_threshold", columnDefinition = "int default 7")
    private Integer reserveDaysThreshold;

    /**
     * 每个用户的任务记录保存数量，最大数量
     */
    @Column(name = "reserve_number_threshold", columnDefinition = "int default 50")
    private Integer reserveNumberThreshold;

    /**
     * CSV导出的一个分页需要几个线程来构造行
     */
    @Column(name = "csv_thread_count", columnDefinition = "int default 3")
    private Integer csvThreadCount;
}
