package io.g740.components.dlock;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author bbottong
 */
@Data
public class DlockDo {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_timestamp")
    private Date createTimestamp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_timestamp")
    private Date lastUpdateTimestamp;

    /**
     * 业务类型，需要作为主键
     */
    @Column(name = "biz_type", length = 100)
    private String bizType;

    /**
     * 超时时长 in minutes
     */
    @Column(name = "timeout_in_minutes")
    private Integer timeoutInMinutes;
}
