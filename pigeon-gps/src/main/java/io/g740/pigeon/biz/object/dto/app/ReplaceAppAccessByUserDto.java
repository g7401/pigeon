package io.g740.pigeon.biz.object.dto.app;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class ReplaceAppAccessByUserDto {
    /**
     * 指定App
     */
    private Long appUid;

    /**
     * 指定App能被哪些用户访问
     */
    private List<String> usernames;
}
