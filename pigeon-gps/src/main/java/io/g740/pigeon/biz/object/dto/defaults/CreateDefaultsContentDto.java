package io.g740.pigeon.biz.object.dto.defaults;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class CreateDefaultsContentDto {
    private String value;
    private String label;
    /**
     * 结构中上一级节点的UID
     */
    private Long parentUid;
    /**
     * 指定默认值类目的UID
     */
    private Long defaultsCategoryUid;
}
