package io.g740.pigeon.biz.object.dto.oauth;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class RefreshedAccessTokenDto {
    private String accessToken;

    /**
     * 令牌失效前剩余的秒数
     */
    private Integer expiresIn;
}
