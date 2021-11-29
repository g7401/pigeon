package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bbottong
 */
@Data
public class DfSimpleDto {
    /**
     * df uid
     */
    private Long dfUid;

    /**
     * df key
     */
    private String dfKey;

    /**
     * df name
     */
    private String dfName;

    /**
     * df description
     */
    private String dfDescription;

    /**
     * df create timestamp
     */
    private Date createTimestamp;

    /**
     * df create by
     */
    private String createBy;

    /**
     * df tag list
     */
    private List<String> tags;
}
