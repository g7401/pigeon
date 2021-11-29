package io.g740.pigeon.biz.persistence.jpa.df;

import io.g740.pigeon.biz.object.entity.df.DfTagDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface DfTagRepository extends PagingAndSortingRepository<DfTagDo, Long> {

    /**
     * 按df uid删除
     *
     * @param dfUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DfTagDo u WHERE u.dfUid = ?1")
    void deleteByDfUid(Long dfUid);

    /**
     * 按df uid查找
     *
     * @param dfUid
     * @return
     */
    List<DfTagDo> findByDfUid(Long dfUid);

    /**
     * 按list of df uid查找
     *
     * @param listOfDfUid
     * @return
     */
    List<DfTagDo> findByDfUidIn(List<Long> listOfDfUid);

    /**
     * 按list of tag查找
     * @param tags
     * @return
     */
    @Query("SELECT u FROM DfTagDo u WHERE u.tag IN ?1 ORDER BY u.dfUid ASC")
    List<DfTagDo> findByTagInAndNameAndOrderByDfUidAsc(List<String> tags);
}