package io.g740.pigeon.biz.object.dto.df;

import io.g740.pigeon.biz.share.constants.DataFieldAggregationTypeEnum;
import io.g740.pigeon.biz.share.constants.DataFieldRoleEnum;
import io.g740.pigeon.biz.share.constants.FormElementTypeEnum;
import lombok.Data;
import org.springframework.data.domain.Sort;

/**
 * @author bbottong
 */
@Data
public class CreateOrReplaceDfConfDataFieldDto {
    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段别名
     */
    private String fieldLabel;

    /**
     * 是否当作查询字段，也就是否当作表单元素
     */
    private Boolean enabledAsFormElement;

    /**
     * 如果当作查询字段，在表单中的表单元素顺序
     */
    private Integer formElementSequence;

    /**
     * 如果当作查询字段，在表单中的表单元素类型
     */
    private FormElementTypeEnum formElementType;

    /**
     * 如果当作查询字段，
     * 并且表单元素类型是，
     * DROP_DOWN_LIST_SINGLE，或者DROP_DOWN_LIST_MULTIPLE，
     * 或者ASSOCIATING_SINGLE，或者ASSOCIATING_MULTIPLE，
     * 则需要绑定字典类目，以便利用字典构建流程按策略生成候选值
     */
    private Long dictionaryCategoryUid;

    /**
     * 如果当作查询字段，并且需要当作几个字段组成的级联关系中的一个，则需要配置下一级的字段名称
     */
    private String childFieldName;

    /**
     * 如果当作查询字段，是否需要启用默认值功能
     */
    private Boolean enabledDefaults;

    /**
     * 如果当作查询字段，并且已启用默认值功能，
     * 则需要绑定默认值类目，以便利用默认值构建流程按策略生成默认值
     */
    private Long defaultsCategoryUid;

    /**
     * 是否当作列表/表格字段
     */
    private Boolean enabledAsListElement;

    /**
     * 如果当作列表/表格字段，在列表/表格中的元素顺序
     */
    private Integer listElementSequence;

    /**
     * 如果当作列表/表格字段，在表格中的元素宽度
     */
    private Integer listElementWidth;

    /**
     * 是否当作排序字段
     */
    private Boolean enabledAsSortElement;

    /**
     * 如果当作排序字段，排序的方向
     */
    private Sort.Direction sortDirection;

    /**
     * 如果当作排序字段，在排序字段组合中的序号
     */
    private Integer sortElementSequence;

    /**
     * 如果当作排序字段，是否强制排序
     */
    private Boolean sortForced;

    /**
     * 如果当作列表/表格字段，该字段在Group By功能中的角色（维度字段/KPI字段）
     */
    private DataFieldRoleEnum role;

    /**
     * 如果当作列表/表格字段，如果role=KPI，则需填写该字段在Group By功能中作为KPI字段所需要的聚合函数
     */
    private DataFieldAggregationTypeEnum aggregationType;

    /**
     * 启用数据权限控制时，该字段对应的resource category uid
     */
    private Long resourceCategoryUid;

    /**
     * 启用数据权限控制时，该字段对应的resource category name
     */
    private String resourceCategoryName;

    /**
     * 启用数据权限控制时，该字段对应的resource category的structure hierarchy上的节点uid
     */
    private Long resourceStructureUid;

    /**
     * 启用数据权限控制时，该字段对应的resource category的structure hierarchy上的节点name
     */
    private String resourceStructureName;
}
