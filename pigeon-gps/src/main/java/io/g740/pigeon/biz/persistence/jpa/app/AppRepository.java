package io.g740.pigeon.biz.persistence.jpa.app;

import io.g740.pigeon.biz.object.entity.app.AppDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bbottong
 */
public interface AppRepository extends PagingAndSortingRepository<AppDo, Long> {
    /**
     * 按app name检查存在
     *
     * @param appName
     * @return
     */
    boolean existsByAppName(String appName);

    /**
     * 按app key检查存在
     *
     * @param appKey
     * @return
     */
    boolean existsByAppKey(String appKey);

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
    @Query("DELETE FROM AppDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);

    /**
     * 按app name和enabled分页查找
     *
     * @param appName
     * @param enabled
     * @param pageable
     * @return
     */
    Page<AppDo> findByAppNameAndEnabled(String appName, Boolean enabled, Pageable pageable);

    /**
     * 按app name分页查找
     *
     * @param appName
     * @param pageable
     * @return
     */
    Page<AppDo> findByAppName(String appName, Pageable pageable);

    /**
     * 按enabled分页查找
     *
     * @param enabled
     * @param pageable
     * @return
     */
    Page<AppDo> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 按app name查找
     *
     * @param appName
     * @return
     */
    AppDo findByAppName(String appName);

    /**
     * 按app key查找
     *
     * @param appKey
     * @return
     */
    AppDo findByAppKey(String appKey);

    /**
     * 按app name和create by分页查找
     *
     * @param appName
     * @param createBy
     * @param pageable
     * @return
     */
    Page<AppDo> findByAppNameAndCreateBy(String appName, String createBy, Pageable pageable);

    /**
     * 按app name和create by分页查找
     *
     * @param createBy
     * @param pageable
     * @return
     */
    Page<AppDo> findByCreateBy(String createBy, Pageable pageable);
}
