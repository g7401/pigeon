package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DfHierarchyFormFieldDto {
    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段描述
     */
    private String fieldDescription;

    /**
     * 字段别名
     */
    private String fieldLabel;

    /**
     * 级联关系中的下一级
     */
    private DfHierarchyFormFieldDto child;
}


