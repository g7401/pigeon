package io.g740.pigeon.biz.object.dto.ds;

import lombok.Data;

/**
 * @author bbottong
 *
 * 测试在数据源执行查询语句
 */
@Data
public class TestQueryStatementDto {
    private Long dsUid;
    private String queryStatement;
}
