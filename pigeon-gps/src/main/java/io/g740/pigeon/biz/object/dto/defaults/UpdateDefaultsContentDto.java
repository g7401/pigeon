package io.g740.pigeon.biz.object.dto.defaults;

import lombok.Data;

/**
 * @author bbottong
 *
 */
@Data
public class UpdateDefaultsContentDto {
    private Long uid;
    private String value;
    private String label;
}
