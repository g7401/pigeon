package io.g740.pigeon.biz.object.dto.account;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateAccountDto {
    private String username;

    private String password;

    private String role;

    private Boolean enabled;
}
