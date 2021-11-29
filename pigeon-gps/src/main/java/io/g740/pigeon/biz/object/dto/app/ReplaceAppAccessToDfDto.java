package io.g740.pigeon.biz.object.dto.app;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class ReplaceAppAccessToDfDto {
    /**
     * 指定App
     */
    private Long appUid;

    /**
     * 指定App能访问哪些Data Facets
     */
    private List<Long> dfUids;
}
