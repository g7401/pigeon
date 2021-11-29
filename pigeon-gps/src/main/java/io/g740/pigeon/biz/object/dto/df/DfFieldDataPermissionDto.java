package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DfFieldDataPermissionDto {
    private String fieldName;
    private Long resourceCategoryUid;
    private String resourceCategoryName;
    private Long resourceStructureUid;
    private String resourceStructureName;
}
