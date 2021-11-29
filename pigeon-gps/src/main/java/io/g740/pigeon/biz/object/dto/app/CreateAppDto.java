package io.g740.pigeon.biz.object.dto.app;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateAppDto {
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
