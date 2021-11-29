package io.g740.components.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface OperationRepository extends PagingAndSortingRepository<OperationDo, Long> {
    /**
     * 灵活查询
     *
     * @param specification
     * @param pageable
     * @return
     */
    Page<OperationDo> findAll(Specification<OperationDo> specification, Pageable pageable);

    @Query("SELECT u FROM OperationDo u WHERE u.createTimestamp < ?1")
    Page<OperationDo> findBeforeCreateTimestamp(java.util.Date createTimestamp, Pageable pageable);

    OperationDo findByRequestId(String requestId);

    /**
     * 删除指定createTimestamp之前的行
     *
     * @param expiredCreateTimestamp
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM OperationDo u WHERE u.createTimestamp <= ?1")
    void deleteBeforeCreateTimestamp(java.util.Date expiredCreateTimestamp);
}
