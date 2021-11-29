package io.g740.pigeon.biz.object.dto.app;

import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import lombok.Data;

/**
 * @author bbottong
 *
 */
@Data
public class AppDfDto extends DfSimpleDto {
    /**
     * Df是否被App选中，true - 选中
     */
    private Boolean selected;
}