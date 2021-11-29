package io.g740.pigeon.biz.object.dto.oauth;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class AccessTokenDto {
    private String accessToken;
    private Integer expiresIn;
    private String refreshToken;
    private String tokenType;
}
