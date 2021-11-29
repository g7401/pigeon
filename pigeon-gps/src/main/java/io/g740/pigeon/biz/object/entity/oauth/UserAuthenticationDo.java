package io.g740.pigeon.biz.object.entity.oauth;

import io.g740.commons.constants.DatabaseConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.uid.tinyid.TinyId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author bbottong
 *
 * access token / 访问令牌
 */
@TinyId(bizType = UserAuthenticationDo.RESOURCE_NAME, beginId = 10, maxId = 10, step = 100, remainder = 0, delta = 1)
@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "bo_user_authentication")
@Where(clause = "is_deleted=" + DatabaseConstants.NOT_DELETED_FLAG)
public class UserAuthenticationDo extends BaseDo {
    public static final String RESOURCE_NAME = "bo_user_authentication";

    private Long uid;

    private String username;

    /**
     * 访问令牌
     */
    @Column(name = "access_token")
    private String accessToken;

    /**
     * 令牌失效前剩余的秒数
     */
    @Column(name = "expires_in")
    private Integer expiresIn;

    /**
     * 令牌失效时间戳
     */
    @Column(name = "expires_at_timestamp")
    private Date expiresAtTimestamp;

    /**
     * 令牌是否已经失效，true - 已失效，false - 没有失效
     */
    @Column(name = "expired")
    private Boolean expired;

    /**
     * 用来获取新的令牌
     */
    @Column(name = "refresh_token")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Column(name = "token_type")
    private String tokenType;
}
