package io.g740.pigeon.biz.object.dto.ds;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class IndexDto {
    /**
     * 索引名称
     */
    private String name;

    /**
     * 是否唯一索引
     */
    private Boolean unique;

    /**
     * 索引包含的列
     */
    private List<IndexColumnDto> columns;
}
