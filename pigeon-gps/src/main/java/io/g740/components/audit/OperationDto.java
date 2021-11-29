package io.g740.components.audit;

import io.g740.commons.types.BaseDto;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class OperationDto extends BaseDto {
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
}
