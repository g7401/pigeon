package io.g740.pigeon.biz.persistence.jpa.df;

import io.g740.pigeon.biz.object.entity.df.DfDataObjectDo;
import io.g740.pigeon.biz.share.constants.DataObjectDependencyTypeEnum;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface DfDataObjectRepository extends PagingAndSortingRepository<DfDataObjectDo, Long> {
    /**
     * 按df uid删除
     *
     * @param dfUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DfDataObjectDo u WHERE u.dfUid = ?1")
    void deleteByDfUid(Long dfUid);

    /**
     * 按df uid查找
     *
     * @param dfUid
     * @return
     */
    List<DfDataObjectDo> findByDfUid(Long dfUid);

    /**
     * 按data object dependency type查找
     *
     * @param dataObjectDependencyType
     * @return
     */
    List<DfDataObjectDo> findByDataObjectDependencyType(DataObjectDependencyTypeEnum dataObjectDependencyType);

    /**
     * 按list of df uid和data object dependency type查找
     *
     * @param listOfDfUid
     * @param dataObjectDependencyType
     * @return
     */
    List<DfDataObjectDo> findByDfUidInAndDataObjectDependencyType(List<Long> listOfDfUid, DataObjectDependencyTypeEnum dataObjectDependencyType);

    /**
     * 按df uid和data object dependency type查找
     *
     * @param dfUid
     * @param dataObjectDependencyType
     * @return
     */
    List<DfDataObjectDo> findByDfUidAndDataObjectDependencyType(
            Long dfUid,
            DataObjectDependencyTypeEnum dataObjectDependencyType);

    /**
     * 按list of data object uid检查存在
     *
     * @param listOfDataObjectUid
     * @return
     */
    boolean existsByDataObjectUidIn(List<Long> listOfDataObjectUid);
}