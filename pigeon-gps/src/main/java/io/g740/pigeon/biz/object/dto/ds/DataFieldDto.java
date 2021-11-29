package io.g740.pigeon.biz.object.dto.ds;

import io.g740.pigeon.biz.share.constants.DataFieldTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DataFieldDto {
    private String fieldName;
    private DataFieldTypeEnum fieldType;
    private Integer fieldLength;
    private String fieldDescription;
}
