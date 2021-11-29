package io.g740.pigeon.biz.object.dto.account;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class UpdateAccountDto {
    private String username;

    private String role;

    private Boolean enabled;
}
