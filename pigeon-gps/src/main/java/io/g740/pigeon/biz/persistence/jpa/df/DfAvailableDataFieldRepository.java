package io.g740.pigeon.biz.persistence.jpa.df;

import io.g740.pigeon.biz.object.entity.df.DfAvailableDataFieldDo;
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
public interface DfAvailableDataFieldRepository extends PagingAndSortingRepository<DfAvailableDataFieldDo, Long> {
    /**
     * 按df uid删除
     *
     * @param dfUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DfAvailableDataFieldDo u WHERE u.dfUid = ?1")
    void deleteByDfUid(Long dfUid);

    /**
     * 按df uid查找
     *
     * @param dfUid
     * @return
     */
    @Query("SELECT u FROM DfAvailableDataFieldDo u WHERE u.dfUid = ?1 ORDER BY u.fieldName ASC")
    List<DfAvailableDataFieldDo> findByDfUidAndOrderByFieldNameAsc(Long dfUid);

    /**
     * 按df uid查找
     *
     * @param dfUid
     * @return
     */
    @Query("SELECT u FROM DfAvailableDataFieldDo u WHERE u.dfUid = ?1 ORDER BY u.ordinalPosition ASC")
    List<DfAvailableDataFieldDo> findByDfUidAndOrderByOrdinalPositionAsc(Long dfUid);

    /**
     * 按df uid检查存在
     *
     * @param dfUid
     * @return
     */
    boolean existsByDfUid(Long dfUid);

    /**
     * 按df uid分页查找
     *
     * @param dfUid
     * @param pageable
     * @return
     */
    Page<DfAvailableDataFieldDo> findByDfUid(Long dfUid, Pageable pageable);
}