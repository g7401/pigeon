package io.g740.pigeon.biz.object.entity.account;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author bbottong
 *
 * User
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_user")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class UserDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_user";

    private String username;

    /**
     * 是否启用
     */
    @Column(name = "enabled", columnDefinition = "boolean default true")
    private Boolean enabled;

    @Column(name = "email_address")
    private String emailAddress;

    private String phone;
}