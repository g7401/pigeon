package io.g740.pigeon.biz.object.dto.df;

import io.g740.pigeon.biz.share.constants.DataFieldAggregationTypeEnum;
import io.g740.pigeon.biz.share.constants.DataFieldRoleEnum;
import io.g740.pigeon.biz.share.constants.DataFieldTypeEnum;
import lombok.Data;
import org.springframework.data.domain.Sort;

/**
 * @author bbottong
 */
@Data
public class DfConfTableFieldDto {
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
     * 字段类型
     */
    private DataFieldTypeEnum fieldType;

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
