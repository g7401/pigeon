package io.g740.pigeon.biz.persistence.jpa.defaults;

import io.g740.pigeon.biz.object.entity.defaults.DefaultsContentDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface DefaultsContentRepository extends PagingAndSortingRepository<DefaultsContentDo, Long> {
    /**
     * 按defaults category uid检查存在
     *
     * @param defaultsCategoryUid
     * @return
     */
    boolean existsByDefaultsCategoryUid(Long defaultsCategoryUid);

    /**
     * 按defaults category uid, value检查存在
     *
     * @param defaultsCategoryUid
     * @param value
     * @return
     */
    boolean existsByDefaultsCategoryUidAndValue(Long defaultsCategoryUid, String value);

    /**
     * 按defaults category uid, label检查存在
     *
     * @param defaultsCategoryUid
     * @param label
     * @return
     */
    boolean existsByDefaultsCategoryUidAndLabel(Long defaultsCategoryUid, String label);

    /**
     * 按defaults category uid和create by删除
     *
     * @param defaultsCategoryUid
     * @param createBy
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DefaultsContentDo u WHERE u.defaultsCategoryUid = ?1 AND u.createBy = ?2")
    void deleteByDefaultsCategoryUidAndCreateBy(Long defaultsCategoryUid, String createBy);

    /**
     * 按defaults category uid删除
     *
     * @param defaultsCategoryUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DefaultsContentDo u WHERE u.defaultsCategoryUid = ?1")
    void deleteByDefaultsCategoryUid(Long defaultsCategoryUid);

    /**
     * 按list of dictionary category uid查找
     *
     * @param listOfDefaultsCategoryUid
     * @return
     */
    @Query("SELECT u FROM DefaultsContentDo u WHERE u.defaultsCategoryUid in ?1")
    List<DefaultsContentDo> findByDefaultsCategoryUidIn(List<Long> listOfDefaultsCategoryUid);

    /**
     * 按dictionary category uid查找
     *
     * @param defaultsCategoryUid
     * @return
     */
    @Query("SELECT u FROM DefaultsContentDo u WHERE u.defaultsCategoryUid = ?1 ORDER BY u.uid ASC")
    List<DefaultsContentDo> findByDefaultsCategoryUidOrderByUidAsc(Long defaultsCategoryUid);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DefaultsContentDo findByUid(Long uid);

    /**
     * 按uid检查存在
     *
     * @param uid
     * @return
     */
    boolean existsByUid(Long uid);

    /**
     * 按uid和create by检查存在
     *
     * @param uid
     * @param createBy
     * @return
     */
    boolean existsByUidAndCreateBy(Long uid, String createBy);

    /**
     * 按uid和create by删除
     *
     * @param uid
     * @param createBy
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DefaultsContentDo u WHERE u.uid = ?1 and u.createBy = ?2")
    void deleteByUidAndCreateBy(Long uid, String createBy);

    /**
     * 按uid删除
     *
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DefaultsContentDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);
}
