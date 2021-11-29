package io.g740.pigeon.biz.object.entity.export;

import io.g740.commons.constants.AsyncJobStatusEnum;
import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import io.g740.pigeon.biz.share.constants.AsyncExportFileTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * Async Export Job / 异步导出工作
 *
 * @author bbottong
 */
@TinyId(bizType = AsyncExportJobDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_bs_async_export_job")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class AsyncExportJobDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_bs_async_export_job";

    @Column(name = "uid")
    private Long uid;

    @Column(name = "name")
    private String name;

    /**
     * 请求类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "request_type")
    private RequestTypeEnum requestType;

    /**
     * 请求Payload
     */
    @Lob
    @Column(name = "request_payload")
    private String requestPayload;

    /**
     * 导出文件类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "content_type")
    private AsyncExportFileTypeEnum contentType;

    /**
     * 执行状态
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "status")
    private AsyncJobStatusEnum status;

    /**
     * 执行进展，执行到第几页，总共多少页
     */
    @Column(name = "progress")
    private String progress;

    /**
     * 执行进展，执行到第几页，总共多少页，百分比
     */
    @Column(name = "progress_percentage")
    private String progressPercentage;

    /**
     * 执行开始时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_timestamp")
    private Date startTimestamp;

    /**
     * 执行完成时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "done_timestamp")
    private Date doneTimestamp;

    /**
     * 执行失败时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "failed_timestamp")
    private Date failedTimestamp;

    /**
     * 执行取消时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "canceled_timestamp")
    private Date canceledTimestamp;

    /**
     * 执行暂停时间戳
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "suspended_timestamp")
    private Date suspendedTimestamp;

    /**
     * 导出文件就绪时间戳（即文件全部内容已生成时）
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "file_ready_timestamp")
    private Date fileReadyTimestamp;

    /**
     * 执行完成后，结果文件将会保存到Shared Storage，并得到一个共享文件ID
     */
    @Column(name = "shared_file_id")
    private String sharedFileId;

    /**
     * 执行完成后，结果文件的名称
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * 总的记录数
     */
    @Column(name = "total")
    private Integer total;

    /**
     * 总的页数
     */
    @Column(name = "total_pages")
    private Integer totalPages;

    /**
     * 每页大小
     */
    @Column(name = "page_size")
    private Integer pageSize;

    /**
     * 执行完成后，结果文件的行数（不包含表头）
     */
    @Column(name = "rows")
    private Integer rows;

    /**
     * 执行完成后，结果文件的大小，字节单位
     */
    @Column(name = "file_length")
    private Long fileLength;

    /**
     * 执行完成后，结果文件的大小，自适应单位：GB/MB/KB/B
     */
    @Column(name = "formatted_file_length", length = 45)
    private String formattedFileLength;

    /**
     * 每行字段数量
     */
    @Column(name = "fields_count")
    private Integer fieldsCount;

    /**
     * 累计查询时长（秒）
     */
    @Column(name = "total_query_duration")
    private Integer totalQueryDuration;

    /**
     * 累计写文件时长（秒）
     */
    @Column(name = "total_write_file_duration")
    private Integer totalWriteFileDuration;

    /**
     * 从创建到完成总耗时（秒）
     */
    @Column(name = "total_duration")
    private Integer totalDuration;

    /**
     * 从创建到完成总耗时（格式化）
     */
    @Column(name = "formatted_total_duration")
    private String formattedTotalDuration;

    public enum RequestTypeEnum {
        DF
    }
}
