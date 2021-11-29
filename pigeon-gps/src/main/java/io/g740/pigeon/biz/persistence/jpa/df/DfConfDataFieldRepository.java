package io.g740.pigeon.biz.persistence.jpa.df;

import io.g740.pigeon.biz.object.entity.df.DfConfDataFieldDo;
import io.g740.pigeon.biz.share.constants.DataFieldRoleEnum;
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
public interface DfConfDataFieldRepository extends PagingAndSortingRepository<DfConfDataFieldDo, Long> {
    /**
     * 按df uid查找
     *
     * @param dfUid
     * @return
     */
    List<DfConfDataFieldDo> findByDfUid(Long dfUid);

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
    @Query("DELETE FROM DfConfDataFieldDo u WHERE u.dfUid = ?1")
    void deleteByDfUid(Long dfUid);

    /**
     * 按df uid分页查找
     *
     * @param dfUid
     * @param pageable
     * @return
     */
    Page<DfConfDataFieldDo> findByDfUid(Long dfUid, Pageable pageable);

    /**
     * 查找指定df的所有启用为Form元素的字段，并按Form元素的升序排列
     *
     * @param dfUid
     * @return
     */
    @Query("SELECT u FROM DfConfDataFieldDo u WHERE u.dfUid = ?1 AND u.enabledAsFormElement = 1 ORDER by " +
            "u.formElementSequence ASC")
    List<DfConfDataFieldDo> findFormDataFieldsByDfUidAndOrderByFormElementSequenceAsc(Long dfUid);

    /**
     * 查找指定df的所有启用为Table元素的字段，并按Table元素的升序排列
     *
     * @param dfUid
     * @return
     */
    @Query("SELECT u FROM DfConfDataFieldDo u WHERE u.dfUid = ?1 AND u.enabledAsListElement = 1 ORDER BY " +
            "u.listElementSequence ASC")
    List<DfConfDataFieldDo> findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(Long dfUid);

    /**
     * 查找指定df的所有启用为Sort元素的字段，并按Sort元素的升序排列
     *
     * @param dfUid
     * @return
     */
    @Query("SELECT u FROM DfConfDataFieldDo u WHERE u.dfUid = ?1 AND u.enabledAsSortElement = 1 ORDER BY " +
            "u.sortElementSequence ASC")
    List<DfConfDataFieldDo> findSortDataFieldsByDfUidAndOrderBySortElementSequenceAsc(Long dfUid);

    /**
     * 查找指定df的所有启用为Group元素的字段
     *
     * @param dfUid
     * @return
     */
    @Query("SELECT u FROM DfConfDataFieldDo u WHERE u.dfUid = ?1 AND u.role IS NOT NULL")
    List<DfConfDataFieldDo> findGroupDataFieldsByDfUid(Long dfUid);

    /**
     * 查找指定df的所有启用为Group元素的字段，且要求按指定role查找
     *
     * @param dfUid
     * @return
     */
    @Query("SELECT u FROM DfConfDataFieldDo u WHERE u.dfUid = ?1 AND u.role = ?2")
    List<DfConfDataFieldDo> findGroupDataFieldsByDfUidAndRole(Long dfUid, DataFieldRoleEnum role);
}