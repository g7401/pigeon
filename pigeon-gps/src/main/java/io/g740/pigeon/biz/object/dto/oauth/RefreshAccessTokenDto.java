package io.g740.pigeon.biz.object.dto.oauth;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class RefreshAccessTokenDto {
    private String clientId;
    private String grantType;
    private String refreshToken;
    private String signature;
}
