package io.g740.pigeon.biz.object.dto.df;

import io.g740.commons.constants.CsvExportStrategyEnum;
import io.g740.pigeon.biz.share.constants.ExportTypeEnum;
import io.g740.pigeon.biz.share.constants.QueryTypeEnum;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
public class DfConfAdvancedDto {
    /**
     * 是否启用分页
     */
    private Boolean enabledPagination;

    /**
     * 如果启用分页，默认分页大小
     */
    private Integer defaultPageSize;

    /**
     * 是否启用垂直滚动，如果启用，则在表格高度超过一个阈值之后显示垂直滚动条
     */
    private Boolean enabledVerticalScrolling;

    /**
     * 如果启用垂直滚动，超过多长（单位：vh, viewport height）则显示垂直滚动条
     */
    private Integer verticalScrollingHeightThreshold;

    /**
     * 是否启用"列序"列，如果启用，则在表格的第1列显示"No."列，内容即为表格中行的序号，从1到N。分页时是按延续排序处理，而不是重新排序。
     */
    private Boolean enabledColumnNo;

    /**
     * 是否启用"复选框"列，如果启用，则在表格的左侧第2列（如果已启用"列序"列）或者第1列（如果没有启用"列序"列）显示"复选框"列。
     */
    private Boolean enabledColumnCheckbox;

    /**
     * 是否启用"操作"列，如果启用，则在表格最后1列显示"Operation"列
     */
    private Boolean enabledColumnOperation;

    /**
     * 是否启用冻结从上至下计第1至N行
     */
    private Boolean enabledFreezeTopRows;

    /**
     * 如果启用冻结从上至下计第1至N行，N是多少
     */
    private Integer inclusiveTopRows;

    /**
     * 是否启用冻结从左至右计第1至M列
     */
    private Boolean enabledFreezeLeftColumns;

    /**
     * 是否启用冻结从左至右计第1至M列，M是多少
     */
    private Integer inclusiveLeftColumns;

    /**
     * 是否启用冻结从右至左计第1至P列
     */
    private Boolean enabledFreezeRightColumns;

    /**
     * 是否启用冻结从右至左计第1至P列，P是多少
     */
    private Integer inclusiveRightColumns;

    /**
     * 导出类型
     */
    private ExportTypeEnum exportType;

    /**
     * 查询类型
     */
    private QueryTypeEnum queryType;

    /**
     * 是否启用Group By功能
     */
    private Boolean enabledGroupBy;

    /**
     * 是否启用图形功能
     */
    private Boolean enabledGraph;

    /**
     * 是否启用表单过长折叠功能
     */
    private Boolean enabledFormFolding;

    /**
     * 是否启用Data Facet名称和描述显示功能
     */
    private Boolean enabledDfNameDescription;

    /**
     * 是否启用进入Data Facet页面即默认查询功能
     */
    private Boolean enabledDefaultQuery;

    /**
     * 导出CSV时处理特殊字符的策略
     */
    private CsvExportStrategyEnum csvExportStrategy;
}
