package io.g740.pigeon.biz.object.dto.app;

import io.g740.commons.types.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author bbottong
 */
@EqualsAndHashCode(callSuper=false)
@Data
public class AppDto extends BaseDto {
    private Long uid;

    /**
     * App Name
     */
    private String appName;

    /**
     * App Key
     */
    private String appKey;

    /**
     * App Secret
     */
    private String appSecret;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
