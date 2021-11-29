package io.g740.pigeon.biz.object.entity.df;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * Data Facet, DF, 数据面
 */
@TinyId(bizType = DfDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_df")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class DfDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_df";

    private Long uid;

    /**
     * JPA把key当保留字，这里避开，在用kee代称
     */
    @Column(name = "kee")
    private String key;

    private String name;

    @Lob
    private String description;

    /**
     * 如果DF依存单个表/视图（只有primary data object），则可以选择是否需要处理（就是transformation）。
     * 或者，
     * 如果DF依存同属一个DS的多个表/视图 (除了primary data object，还有secondary data objects)，
     * 则一定需要处理（也是transformation）。
     */
    @Column(name = "processing_needed", columnDefinition = "boolean default false")
    private Boolean processingNeeded;

    /**
     * 如果processingNeeded为true，则表示需要对DF所依存的表/视图执行处理（就是transformation）。
     * 处理逻辑当前只支持SQL语句，执行一段SQL语句，以便经过SQL处理得到转换后的数据格式和内容。
     */
    @Lob
    @Column(name = "processing_logic")
    private String processingLogic;
}
