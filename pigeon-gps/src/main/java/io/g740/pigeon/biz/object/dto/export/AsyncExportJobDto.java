package io.g740.pigeon.biz.object.dto.export;

import io.g740.commons.constants.AsyncJobStatusEnum;
import io.g740.commons.types.BaseDto;
import io.g740.pigeon.biz.object.entity.export.AsyncExportJobDo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 异步导出工作
 *
 * @author bbottong
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class AsyncExportJobDto extends BaseDto {
    private Long uid;

    private String name;

    /**
     * 请求类型
     */
    private AsyncExportJobDo.RequestTypeEnum requestType;

    /**
     * 执行状态
     */
    private AsyncJobStatusEnum status;

    /**
     * 执行进展，执行到第几页，总共多少页
     */
    private String progress;

    /**
     * 执行进展，执行到第几页，总共多少页，百分比
     */
    private String progressPercentage;

    /**
     * 执行开始时间戳
     */
    private Date startTimestamp;

    /**
     * 执行完成时间戳
     */
    private Date doneTimestamp;

    /**
     * 执行失败时间戳
     */
    private Date failedTimestamp;

    /**
     * 执行取消时间戳
     */
    private Date canceledTimestamp;

    /**
     * 执行暂停时间戳
     */
    private Date suspendedTimestamp;

    /**
     * 导出文件就绪时间戳（即文件全部内容已生成时）
     */
    private Date fileReadyTimestamp;

    /**
     * 执行完成后，结果文件将会保存到Shared Storage，并得到一个共享文件ID
     */
    private String sharedFileId;

    /**
     * 执行完成后，结果文件的名称
     */
    private String fileName;

    /**
     * 总的记录数
     */
    private Integer total;

    /**
     * 总的页数
     */
    private Integer totalPages;
    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 执行完成后，结果文件的行数（不包含表头）
     */
    private Integer rows;

    /**
     * 执行完成后，结果文件的大小，字节单位
     */
    private Long fileLength;

    /**
     * 执行完成后，结果文件的大小，自适应单位：GB/MB/KB/B
     */
    private String formattedFileLength;

    /**
     * 每行字段数量
     */
    private Integer fieldsCount;

    /**
     * 累计查询时长（秒）
     */
    private Integer totalQueryDuration;

    /**
     * 累计写文件时长（秒）
     */
    private Integer totalWriteFileDuration;

    /**
     * 从创建到完成总耗时（秒）
     */
    private Integer totalDuration;

    /**
     * 从创建到完成总耗时（格式化）
     */
    private String formattedTotalDuration;
}
