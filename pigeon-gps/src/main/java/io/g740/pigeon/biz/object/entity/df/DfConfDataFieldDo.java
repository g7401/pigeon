package io.g740.pigeon.biz.object.entity.df;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.pigeon.biz.share.constants.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Sort;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * DF的配置字段，只记录要么作为查询字段，要么作为列表/表格字段，要么两者都有的字段
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_df_conf_data_field")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DfConfDataFieldDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_df_conf_data_field";

    public static final String DEFAULT_SORT_ORDER_DIRECTION = "ASC";

    @Column(name = "df_uid")
    private Long dfUid;

    /**
     * 字段名称
     */
    @Column(name = "field_name")
    private String fieldName;

    /**
     * 字段类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "field_type")
    private DataFieldTypeEnum fieldType;

    /**
     * 字段长度，只有TEXT类型需要记录字段长度
     */
    @Column(name = "field_length")
    private Integer fieldLength;

    /**
     * 字段描述
     */
    @Column(name = "field_description")
    private String fieldDescription;

    /**
     * 字段别名
     */
    @Column(name = "field_label")
    private String fieldLabel;

    /**
     * 是否当作查询字段，也就是否当作表单元素
     */
    @Column(name = "enabled_as_form_element")
    private Boolean enabledAsFormElement;

    /**
     * 如果当作查询字段，在表单中的表单元素顺序
     */
    @Column(name = "form_element_sequence")
    private Integer formElementSequence;

    /**
     * 如果当作查询字段，在表单中的表单元素类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "form_element_type")
    private FormElementTypeEnum formElementType;

    /**
     * 如果当作查询字段，
     * 并且表单元素类型是，
     * DROP_DOWN_LIST_SINGLE，或者DROP_DOWN_LIST_MULTIPLE，
     * 或者ASSOCIATING_SINGLE，或者ASSOCIATING_MULTIPLE，
     * 则需要绑定字典类目，以便利用字典构建流程按策略生成候选值
     */
    @Column(name = "dictionary_category_uid")
    private Long dictionaryCategoryUid;

    /**
     * 如果当作查询字段，并且需要当作几个字段组成的级联关系中的一个，则需要配置下一级的字段名称
     */
    @Column(name = "child_field_name")
    private String childFieldName;

    /**
     * 如果当作查询字段，并且已启用默认值功能，
     * 则需要绑定默认值类目，以便利用默认值构建流程按策略生成默认值
     */
    @Column(name = "defaults_category_uid")
    private Long defaultsCategoryUid;

    /**
     * 是否当作列表/表格字段
     */
    @Column(name = "enabled_as_list_element")
    private Boolean enabledAsListElement;

    /**
     * 如果当作列表/表格字段，在列表/表格中的元素顺序
     */
    @Column(name = "list_element_sequence")
    private Integer listElementSequence;

    /**
     * 如果当作列表/表格字段，在表格中的元素宽度
     */
    @Column(name = "list_element_width")
    private Integer listElementWidth;

    /**
     * 是否当作排序字段
     */
    @Column(name = "enabled_as_sort_element")
    private Boolean enabledAsSortElement;

    /**
     * 如果当作排序字段，排序的方向
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "sort_direction")
    private Sort.Direction sortDirection;

    /**
     * 如果当作排序字段，在排序字段组合中的序号
     */
    @Column(name = "sort_element_sequence")
    private Integer sortElementSequence;

    /**
     * 如果当作排序字段，是否强制排序
     */
    @Column(name = "sort_forced")
    private Boolean sortForced;

    /**
     * 不管是否当作列表/表格字段，该字段在Group By功能中的角色（维度字段/KPI字段）
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "role")
    private DataFieldRoleEnum role;

    /**
     * 不管是否当作列表/表格字段，如果role=KPI，则需填写该字段在Group By功能中作为KPI字段所需要的聚合函数
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "aggregation_type")
    private DataFieldAggregationTypeEnum aggregationType;

    /**
     * 启用数据权限控制时，该字段对应的resource category uid
     */
    @Column(name = "resource_category_uid")
    private Long resourceCategoryUid;

    /**
     * 启用数据权限控制时，该字段对应的resource category name
     */
    @Column(name = "resource_category_name")
    private String resourceCategoryName;

    /**
     * 启用数据权限控制时，该字段对应的resource category的structure hierarchy上的节点uid
     */
    @Column(name = "resource_structure_uid")
    private Long resourceStructureUid;

    /**
     * 启用数据权限控制时，该字段对应的resource category的structure hierarchy上的节点name
     */
    @Column(name = "resource_structure_name")
    private String resourceStructureName;

    /**
     * 字段在所有字段中的序号（从0开始计数）
     */
    @Column(name = "ordinal_position")
    private Integer ordinalPosition;
}