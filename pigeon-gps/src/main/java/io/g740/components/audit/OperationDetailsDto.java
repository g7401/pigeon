package io.g740.components.audit;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class OperationDetailsDto extends OperationDto {
    private List<OperationTaskDto> tasks;
}
