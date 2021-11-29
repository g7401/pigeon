package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class DfAdvancedQueryDto {
    private List<String> requiredReturnFieldNames;
    private DfQueryGroupByDto groupBy;
}
