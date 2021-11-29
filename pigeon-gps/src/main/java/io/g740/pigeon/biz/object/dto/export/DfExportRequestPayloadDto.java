package io.g740.pigeon.biz.object.dto.export;

import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * Data Facet 内容导出 request payload
 *
 * @author bbottong
 */
@Data
public class DfExportRequestPayloadDto {
    private String countSqlStatement;
    private String querySqlStatement;
    private DsTypeEnum dsType;
    private String connectionProfileProps;
    private List<String> requiredDataFieldNamesInOrder;
}
