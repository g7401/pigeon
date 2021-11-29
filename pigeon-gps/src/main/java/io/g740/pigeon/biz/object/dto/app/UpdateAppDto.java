package io.g740.pigeon.biz.object.dto.app;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class UpdateAppDto {
    private Long uid;

    /**
     * App Name
     */
    private String appName;

    /**
     * App Secret
     */
    private String appSecret;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
