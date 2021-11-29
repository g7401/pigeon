package io.g740.pigeon.base.object.entity;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * 系统发布信息
 */
@TinyId(bizType = SystemReleaseDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "to_sys_release")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class SystemReleaseDo extends BaseDo {
    public static final String RESOURCE_NAME = "sys_release";

    private Long uid;

    @Column(name = "terms_of_service_url")
    private String termsOfServiceUrl;

    @Column(name = "privacy_policy_url")
    private String privacyPolicyUrl;

    @Column(name = "release_version")
    private String releaseVersion;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "enabled")
    private Boolean enabled;
}
