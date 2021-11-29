package io.g740.components.audit;

import lombok.Data;

@Data
public class OperationTaskInputDto {
    /**
     * RequestIdLogFilter 分配的 requestId
     */
    private String requestId;

    /**
     * Request Uri
     */
    private String requestUri;

    /**
     * Parameters
     */
    private String parameters;

    /**
     * 任务
     */
    private String task;

    /**
     * 输入
     */
    private String input;

    /**
     * 输出
     */
    private String output;

    private java.util.Date createTimestamp;
}
