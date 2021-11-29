package io.g740.components.job;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class JobFailedReasonDto {
    private Long uid;
    private String jobCategory;
    private Long jobUid;
    private String content;
}
