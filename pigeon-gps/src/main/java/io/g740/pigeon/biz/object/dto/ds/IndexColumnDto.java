package io.g740.pigeon.biz.object.dto.ds;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class IndexColumnDto {
    /**
     * 列名
     */
    private String columnName;

    /**
     * 该列在索引中的序号，从1开始计数
     */
    private Integer sequence;
}
