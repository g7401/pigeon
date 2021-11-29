package io.g740.components.job;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * @author bbottong
 *
 * 数据源
 */
@TinyId(bizType = JobFailedReasonDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_bs_job_failed_reason")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class JobFailedReasonDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_bs_job_failed_reason";

    public static final Integer MAX_LENGTH = 3096;

    private Long uid;

    /**
     * job category
     */
    @Column(name = "job_category")
    private String jobCategory;

    /**
     * job uid
     */
    @Column(name = "job_uid")
    private Long jobUid;

    /**
     * job instance failed reason
     */
    @Lob
    @Column(name = "content")
    private String content;
}
