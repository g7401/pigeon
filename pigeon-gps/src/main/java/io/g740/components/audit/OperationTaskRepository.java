package io.g740.components.audit;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface OperationTaskRepository extends PagingAndSortingRepository<OperationTaskDo, Long> {

    /**
     * 按request id删除
     *
     * @param requestId
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM OperationTaskDo u WHERE u.requestId = ?1")
    void deleteByRequestId(String requestId);

    /**
     * 按request id删除
     *
     * @param requestIds
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM OperationTaskDo u WHERE u.requestId IN ?1")
    void deleteByRequestIdIn(List<String> requestIds);


    @Query("SELECT u FROM OperationTaskDo u WHERE u.requestId = ?1 ORDER BY u.createTimestamp ASC")
    List<OperationTaskDo> findByRequestIdWithOrderByCreateTimestampAsc(String requestId);

    /**
     * 删除指定createTimestamp之前的行
     *
     * @param expiredCreateTimestamp
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM OperationTaskDo u WHERE u.createTimestamp <= ?1")
    void deleteBeforeCreateTimestamp(java.util.Date expiredCreateTimestamp);
}
