package io.g740.components.audit;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
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
 * operation
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_bs_operation")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class OperationDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_bs_operation";

    public static final String COMMON_TASK_API_INCOMING = "api_incoming";
    public static final String COMMON_TASK_API_OUTGOING = "api_outgoing";

    /**
     * RequestIdLogFilter 分配的 requestId
     */
    @Column(name = "request_id")
    private String requestId;

    /**
     * Request Uri
     */
    @Column(name = "request_uri", length = 1000)
    private String requestUri;

    /**
     * Parameters
     */
    @Lob
    @Column(name = "parameters")
    private String parameters;
}
