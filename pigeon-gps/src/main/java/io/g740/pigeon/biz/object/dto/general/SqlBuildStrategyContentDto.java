package io.g740.pigeon.biz.object.dto.general;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class SqlBuildStrategyContentDto {
    /**
     * 数据源的UID
     */
    private Long dsUid;

    /**
     * SQL语句
     */
    private String sqlStatement;
}
