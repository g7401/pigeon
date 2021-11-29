package io.g740.pigeon.biz.persistence.jpa.app;

import io.g740.pigeon.biz.object.entity.app.AppDfDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface AppDfRepository extends PagingAndSortingRepository<AppDfDo, Long> {
    /**
     * 按app uid检查存在
     *
     * @param appUid
     * @return
     */
    boolean existsByAppUid(Long appUid);

    /**
     * 按app uid查找df uid
     *
     * @param appUid
     * @return
     */
    @Query("SELECT u.dfUid FROM AppDfDo u WHERE u.appUid=?1")
    List<Long> findDfUidByAppUid(Long appUid);

    /**
     * 按app uid分页查找
     *
     * @param appUid
     * @param pageable
     * @return
     */
    Page<AppDfDo> findByAppUid(Long appUid, Pageable pageable);

    /**
     * 按app uid查找
     *
     * @param appUid
     * @return
     */
    List<AppDfDo> findByAppUid(Long appUid);

    /**
     * 按app uid删除
     *
     * @param appUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM AppDfDo u WHERE u.appUid = ?1")
    void deleteByAppUid(Long appUid);

    /**
     * 按list of app uid查找df uid
     *
     * @param listOfAppUid
     * @return
     */
    @Query("SELECT u.dfUid FROM AppDfDo u WHERE u.appUid IN ?1")
    List<Long> findDfUidByAppUidIn(List<Long> listOfAppUid);

    /**
     * 按app uid和df uid检查存在
     *
     * @param appUid
     * @param dfUid
     * @return
     */
    boolean existsByAppUidAndDfUid(Long appUid, Long dfUid);
}
