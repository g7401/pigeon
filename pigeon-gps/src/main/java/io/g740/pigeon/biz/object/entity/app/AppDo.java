package io.g740.pigeon.biz.object.entity.app;

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
 * App
 */
@TinyId(bizType = AppDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_app")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class AppDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_app";

    private Long uid;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "app_key")
    private String appKey;

    @Column(name = "app_secret")
    private String appSecret;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled;
}
