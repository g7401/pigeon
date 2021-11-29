package io.g740.pigeon.biz.object.dto.df;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.g740.pigeon.biz.object.dto.serialization.DfQueryGroupByTypeEnumDeserializer;
import io.g740.pigeon.biz.share.constants.DfQueryGroupByFieldProcessingTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class DfQueryGroupByDto {
    /**
     * 选定进行group by的字段，含顺序
     */
    private List<String> selectedGroupByFieldNames;

    /**
     * group by功能的选定字段处理类型
     */
    @JsonDeserialize(using = DfQueryGroupByTypeEnumDeserializer.class)
    private DfQueryGroupByFieldProcessingTypeEnum fieldProcessingType;

    /**
     * 如果type选定的是"ADVANCED"，则需选择结果字段
     */
    private List<DfQueryResultFieldDto> resultFields;

    /**
     * 如果type选定的是"ADVANCED"，则可选择HAVING字段
     */
    private List<DfQueryHavingFieldDto> havingFields;
}
