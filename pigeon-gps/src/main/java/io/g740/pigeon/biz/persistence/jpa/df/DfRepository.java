package io.g740.pigeon.biz.persistence.jpa.df;

import io.g740.pigeon.biz.object.entity.df.DfDo;
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
public interface DfRepository extends PagingAndSortingRepository<DfDo, Long> {
    /**
     * 按uid检查存在
     *
     * @param uid
     * @return
     */
    boolean existsByUid(Long uid);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DfDo findByUid(Long uid);

    /**
     * 按createBy查找
     *
     * @param username
     * @return
     */
    List<DfDo> findByCreateBy(String username);

    /**
     * 按uid删除
     *
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DfDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);

    /**
     * 按key检查存在
     *
     * @param key
     * @return
     */
    boolean existsByKey(String key);

    /**
     * 按name检查存在
     *
     * @param name
     * @return
     */
    boolean existsByName(String name);

    /**
     * 按key查找
     *
     * @param key
     * @return
     */
    DfDo findByKey(String key);

    /**
     * 按key删除
     *
     * @param key
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DfDo u WHERE u.key = ?1")
    void deleteByKey(String key);

    /**
     * 按list of uid分页查找
     *
     * @param listOfUid
     * @param pageable
     * @return
     */
    Page<DfDo> findByUidIn(List<Long> listOfUid, Pageable pageable);

    /**
     * 按list of uid查找
     *
     * @param listOfUid
     * @return
     */
    List<DfDo> findByUidIn(List<Long> listOfUid);

    /**
     * 分页查找
     *
     * @param pageable
     * @return
     */
    @Override
    Page<DfDo> findAll(Pageable pageable);
}