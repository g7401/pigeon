package io.g740.pigeon.biz.object.dto.df;

import io.g740.pigeon.biz.share.constants.QueryConditionTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DfQueryHavingFieldDto {
    /**
     * new field label
     */
    private String newFieldLabel;

    /**
     * 查询条件类型
     */
    private QueryConditionTypeEnum queryConditionType;

    /**
     * 值
     */
    private String values;
}
