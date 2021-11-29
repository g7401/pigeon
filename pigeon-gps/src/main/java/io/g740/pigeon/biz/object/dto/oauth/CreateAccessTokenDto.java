package io.g740.pigeon.biz.object.dto.oauth;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateAccessTokenDto {
    private String clientId;
    private String grantType;
    private String signature;
}
