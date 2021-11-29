package io.g740.pigeon.biz.persistence.jpa.ds;

import io.g740.pigeon.biz.object.entity.ds.DsDataObjectDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface DsDataObjectRepository extends PagingAndSortingRepository<DsDataObjectDo, Long> {

    /**
     * 按uid检查存在
     *
     * @param uid
     * @return
     */
    boolean existsByUid(Long uid);

    /**
     * 按list of uid查找
     *
     * @param uid
     * @return
     */
    List<DsDataObjectDo> findByUidIn(List<Long> uid);

    /**
     * 按ds uid查找
     *
     * @param dsUid
     * @return
     */
    List<DsDataObjectDo> findByDsUid(Long dsUid);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DsDataObjectDo findByUid(Long uid);

    /**
     * 按ds uid删除
     *
     * @param dsUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DsDataObjectDo u WHERE u.dsUid = ?1")
    void deleteByDsUid(Long dsUid);

    /**
     * 按list of ds uid查找
     *
     * @param dsUid
     * @return
     */
    List<DsDataObjectDo> findByDsUidIn(List<Long> dsUid);
}
