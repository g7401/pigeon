package io.g740.pigeon.biz.object.dto.account;

import lombok.Data;

import java.util.Date;

/**
 * @author bbottong
 */
@Data
public class AccountProfileDto {
    private String username;

    private String role;

    private Boolean enabled;

    private String emailAddress;

    private String phone;

    private Date createTimestamp;
}
