package io.g740.pigeon.biz.share.constants;

/**
 * Group By功能的字段处理类型
 *
 * @author bbottong
 */
public enum DfQueryGroupByFieldProcessingTypeEnum {
    /**
     * 显示两类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，且默认采用加和统计；
     */
    RETURN_ONLY_SELECTED_FIELDS,
    /**
     * 显示三类字段：1）已选中备选字段（即已选中维度字段）；2）KPI字段，且默认采用加和统计；3）剩余字段，默认返回空值；
     */
    RETURN_ALL_FIELDS,
    /**
     * 高级功能
     */
    ADVANCED
}
