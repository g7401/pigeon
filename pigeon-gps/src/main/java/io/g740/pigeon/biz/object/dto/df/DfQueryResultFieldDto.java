package io.g740.pigeon.biz.object.dto.df;

import io.g740.pigeon.biz.share.constants.DfQueryGroupByAggregateFunctionTypeEnum;
import lombok.Data;
import org.springframework.data.domain.Sort;

/**
 * @author bbottong
 */
@Data
public class DfQueryResultFieldDto {
    /**
     * field name
     */
    private String fieldName;

    /**
     * 针对field name的聚合函数类型
     */
    private DfQueryGroupByAggregateFunctionTypeEnum aggregateFunctionType;

    /**
     * 针对field name进行聚合后的new field label，而new field name系统自己确定，不对外显示
     */
    private String newFieldLabel;

    /**
     * 针对field name进行聚合后的新字段，怎么排序
     */
    private Sort.Direction sortDirection;
}
