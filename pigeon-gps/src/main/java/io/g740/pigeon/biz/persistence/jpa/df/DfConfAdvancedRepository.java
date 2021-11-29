package io.g740.pigeon.biz.persistence.jpa.df;

import io.g740.pigeon.biz.object.entity.df.DfConfAdvancedDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bbottong
 */
public interface DfConfAdvancedRepository extends PagingAndSortingRepository<DfConfAdvancedDo, Long> {
    /**
     * 按df uid查找
     *
     * @param uid
     * @return
     */
    DfConfAdvancedDo findByDfUid(Long uid);

    /**
     * 按df uid检查存在
     *
     * @param dfUid
     * @return
     */
    boolean existsByDfUid(Long dfUid);

    /**
     * 按df uid删除
     *
     * @param dfUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DfConfAdvancedDo u WHERE u.dfUid = ?1")
    void deleteByDfUid(Long dfUid);
}