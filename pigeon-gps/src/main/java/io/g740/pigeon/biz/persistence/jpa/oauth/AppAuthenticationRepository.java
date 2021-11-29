package io.g740.pigeon.biz.persistence.jpa.oauth;

import io.g740.pigeon.biz.object.entity.oauth.AppAuthenticationDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface AppAuthenticationRepository extends PagingAndSortingRepository<AppAuthenticationDo, Long> {
    /**
     * 按app key和refresh token检查存在
     *
     * @param appKey
     * @param refreshToken
     * @return
     */
    boolean existsByAppKeyAndRefreshToken(String appKey, String refreshToken);

    /**
     * 按app key查找还未过期的
     *
     * @param appKey
     * @return
     */
    @Query("SELECT u FROM AppAuthenticationDo u WHERE u.appKey = ?1 AND u.expired = 0")
    List<AppAuthenticationDo> findUnexpiredByAppKey(String appKey);


    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    AppAuthenticationDo findByUid(Long uid);

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
    @Query("DELETE FROM AppAuthenticationDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);

    /**
     * 按app key和refresh token查找
     *
     * @param appKey
     * @param refreshToken
     * @return
     */
    AppAuthenticationDo findByAppKeyAndRefreshToken(String appKey, String refreshToken);
}
