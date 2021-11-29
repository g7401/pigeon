package io.g740.pigeon.biz.persistence.jpa.ds;

import io.g740.pigeon.biz.object.entity.ds.DsDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface DsRepository extends PagingAndSortingRepository<DsDo, Long> {
    /**
     * 按name检查存在
     *
     * @param name
     * @return
     */
    boolean existsByName(String name);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DsDo findByUid(Long uid);

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
    @Query("DELETE FROM DsDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);

    /**
     * 按list of uid查找
     *
     * @param listOfUid
     * @return
     */
    List<DsDo> findByUidIn(List<Long> listOfUid);

    /**
     * 按connection profile props检查存在
     *
     * @param connectionProfileProps
     * @return
     */
    boolean existsByConnectionProfileProps(String connectionProfileProps);

    /**
     * 按create by查找
     * 
     * @param username
     * @return
     */
    List<DsDo> findByCreateBy(String username);
}
