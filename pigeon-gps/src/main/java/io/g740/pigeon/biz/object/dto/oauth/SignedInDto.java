package io.g740.pigeon.biz.object.dto.oauth;

import lombok.Data;

import java.util.Date;

/**
 * @author bbottong
 */
@Data
public class SignedInDto {
    private String username;

    /**
     * 令牌
     */
    private String accessToken;

    /**
     * 令牌失效之前剩余的秒数
     */
    private Integer expiresIn;

    /**
     * 令牌失效时间戳
     */
    private Date expiresAtTimestamp;

    private String refreshToken;

    private String tokenType;
}
