package io.g740.pigeon.biz.object.dto.df;

import io.g740.pigeon.biz.object.dto.defaults.SimpleDefaultsDto;
import io.g740.pigeon.biz.share.constants.FormElementTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class DfConfFormFieldDto {
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
     * 在表单中的表单元素顺序
     */
    private Integer formElementSequence;

    /**
     * 在表单中的表单元素类型
     */
    private FormElementTypeEnum formElementType;

    /**
     * 默认值可以有一个或多个，null表示没有默认值。
     */
    private List<SimpleDefaultsDto> fieldDefaultValues;

    /**
     * 候选值。下拉列表或者联想输入需要1个或多个候选值，仍然用TreeNode表达，不过除了ROOT层级以外只有1个层级
     */
    private SimpleTreeNode fieldOptionalValues;

    /**
     * 如果本字段是M (M > 1)个字段所组成的M级联关系中的第一级字段，其后M-1个字段（也就是M-1级）包含在fieldHierarchyStructure中，
     * 不再以本字段并列出现。
     */
    private DfHierarchyFormFieldDto fieldHierarchyStructure;

    /**
     * 字典category uid
     */
    private Long dictionaryCategoryUid;

    /**
     * 字典category名称
     */
    private String dictionaryCategoryName;

}

