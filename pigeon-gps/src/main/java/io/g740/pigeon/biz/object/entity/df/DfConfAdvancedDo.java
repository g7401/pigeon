package io.g740.pigeon.biz.object.entity.df;

import io.g740.commons.constants.CsvExportStrategyEnum;
import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.pigeon.biz.share.constants.ExportTypeEnum;
import io.g740.pigeon.biz.share.constants.QueryTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * DF的高级配置
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_df_conf_advanced")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DfConfAdvancedDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_df_conf_advanced";

    @Column(name = "df_uid")
    private Long dfUid;

    /**
     * 是否启用分页
     */
    @Column(name = "enabled_pagination", columnDefinition = "boolean default true")
    private Boolean enabledPagination;

    /**
     * 如果启用分页，默认分页大小
     */
    @Column(name = "default_page_size", columnDefinition = "int default 100")
    private Integer defaultPageSize;

    /**
     * 是否启用垂直滚动，如果启用，则在表格高度超过一个阈值之后显示垂直滚动条
     */
    @Column(name = "enabled_vertical_scrolling", columnDefinition = "boolean default false")
    private Boolean enabledVerticalScrolling;

    /**
     * 如果启用垂直滚动，超过多长（单位：vh, viewport height）则显示垂直滚动条
     */
    @Column(name = "vertical_scrolling_height_threshold", columnDefinition = "int default 800")
    private Integer verticalScrollingHeightThreshold;

    /**
     * 是否启用"列序"列，如果启用，则在表格的第1列显示"No."列，内容即为表格中行的序号，从1到N。分页时是按延续排序处理，而不是重新排序。
     */
    @Column(name = "enabled_column_no", columnDefinition = "boolean default true")
    private Boolean enabledColumnNo;

    /**
     * 是否启用"复选框"列，如果启用，则在表格的左侧第2列（如果已启用"列序"列）或者第1列（如果没有启用"列序"列）显示"复选框"列。
     */
    @Column(name = "enabled_column_checkbox", columnDefinition = "boolean default false")
    private Boolean enabledColumnCheckbox;

    /**
     * 是否启用"操作"列，如果启用，则在表格最后1列显示"Operation"列
     */
    @Column(name = "enabled_column_operation", columnDefinition = "boolean default false")
    private Boolean enabledColumnOperation;

    /**
     * 是否启用冻结从上至下计第1至N行
     */
    @Column(name = "enabled_freeze_top_rows", columnDefinition = "boolean default false")
    private Boolean enabledFreezeTopRows;

    /**
     * 如果启用冻结从上至下计第1至N行，N是多少
     */
    @Column(name = "inclusive_top_rows")
    private Integer inclusiveTopRows;

    /**
     * 是否启用冻结从左至右计第1至M列
     */
    @Column(name = "enabled_freeze_left_columns", columnDefinition = "boolean default false")
    private Boolean enabledFreezeLeftColumns;

    /**
     * 是否启用冻结从左至右计第1至M列，M是多少
     */
    @Column(name = "inclusive_left_columns")
    private Integer inclusiveLeftColumns;

    /**
     * 是否启用冻结从右至左计第1至P列
     */
    @Column(name = "enabled_freeze_right_columns")
    private Boolean enabledFreezeRightColumns;

    /**
     * 是否启用冻结从右至左计第1至P列，P是多少
     */
    @Column(name = "inclusive_right_columns")
    private Integer inclusiveRightColumns;

    /**
     * 导出类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "export_type")
    private ExportTypeEnum exportType;

    /**
     * 查询类型
     */
    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "query_type")
    private QueryTypeEnum queryType;

    /**
     * 是否启用Group By功能
     */
    @Column(name = "enabled_group_by", columnDefinition = "boolean default false")
    private Boolean enabledGroupBy;

    /**
     * 是否启用图形功能
     */
    @Column(name = "enabled_graph", columnDefinition = "boolean default false")
    private Boolean enabledGraph;

    /**
     * 是否启用表单过长折叠功能
     */
    @Column(name = "enabled_form_folding", columnDefinition = "boolean default false")
    private Boolean enabledFormFolding;

    /**
     * 是否启用Data Facet名称和描述显示功能
     */
    @Column(name = "enabled_df_name_description", columnDefinition = "boolean default true")
    private Boolean enabledDfNameDescription;

    /**
     * 是否启用进入Data Facet页面即默认查询功能
     */
    @Column(name = "enabled_default_query", columnDefinition = "boolean default true")
    private Boolean enabledDefaultQuery;

    /**
     * 导出CSV时处理特殊字符的策略
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "csv_export_strategy", columnDefinition = "int default 2")
    private CsvExportStrategyEnum csvExportStrategy;
}