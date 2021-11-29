package io.g740.pigeon.biz.object.entity.account;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

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
@Table(name = "bo_user_membership")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class MembershipDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_user_membership";

    private String username;

    private String role;
}