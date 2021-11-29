package io.g740.pigeon.biz.share.types;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Data
public class SimpleQueryResult {
    /**
     * 列名及列序
     */
    private List<String> columnNames;
    /**
     * 行集合
     */
    private List<Map<String, Object>> rows;
}
