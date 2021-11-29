package io.g740.pigeon.biz.persistence.jpa.oauth;

import io.g740.pigeon.biz.object.entity.app.AppDo;
import io.g740.pigeon.biz.object.entity.oauth.UserAuthenticationDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface UserAuthenticationRepository extends PagingAndSortingRepository<UserAuthenticationDo, Long> {
    /**
     * 按username和refresh token检查存在
     *
     * @param username
     * @param refreshToken
     * @return
     */
    boolean existsByUsernameAndRefreshToken(String username, String refreshToken);

    /**
     * 按username查找还未过期的
     *
     * @param username
     * @return
     */
    @Query("SELECT u FROM UserAuthenticationDo u WHERE u.username = ?1 AND u.expired = 0")
    List<UserAuthenticationDo> findUnexpiredByUsername(String username);


    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    AppDo findByUid(Long uid);

    /**
     * 按uid检查，是否存在
     *
     * @param uid
     * @return
     */
    boolean existsByUid(Long uid);

    /**
     * 按uid删除
     *
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM UserAuthenticationDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);

    /**
     * 按username和refresh token查找
     *
     * @param username
     * @param refreshToken
     * @return
     */
    UserAuthenticationDo findByUsernameAndRefreshToken(String username, String refreshToken);
}
