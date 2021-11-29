package io.g740.pigeon.biz.object.entity.app;

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
 * Who has access to the specific app
 */
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_app_user")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class AppUserDo extends BaseDo {
    @Column(name = "app_uid")
    private Long appUid;

    @Column(name = "username")
    private String username;
}