package io.g740.components.audit;

import io.g740.commons.types.BaseDto;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class OperationTaskDto extends BaseDto {
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
}
