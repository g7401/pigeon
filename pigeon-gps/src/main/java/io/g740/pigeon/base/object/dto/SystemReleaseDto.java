package io.g740.pigeon.base.object.dto;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class SystemReleaseDto {
    private String termsOfServiceUrl;
    private String privacyPolicyUrl;
    private String releaseVersion;
    private String vendorName;
}
