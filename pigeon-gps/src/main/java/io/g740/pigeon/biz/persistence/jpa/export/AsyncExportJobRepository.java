package io.g740.pigeon.biz.persistence.jpa.export;

import io.g740.commons.constants.AsyncJobStatusEnum;
import io.g740.pigeon.biz.object.entity.export.AsyncExportJobDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author bbottong
 */
public interface AsyncExportJobRepository extends PagingAndSortingRepository<AsyncExportJobDo, Long> {

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    AsyncExportJobDo findByUid(Long uid);

    /**
     * 按uid和create by查找
     *
     * @param uid
     * @param createBy
     * @return
     */
    AsyncExportJobDo findByUidAndCreateBy(Long uid, String createBy);

    /**
     * 按create by查找
     *
     * @param createBy
     * @param pageable
     * @return
     */
    Page<AsyncExportJobDo> findByCreateBy(String createBy, Pageable pageable);

    /**
     * 按状态查找
     *
     * @param status
     * @param pageable
     * @return
     */
    Page<AsyncExportJobDo> findByStatus(AsyncJobStatusEnum status, Pageable pageable);

    /**
     * 按状态COUNT
     *
     * @param status
     * @return
     */
    @Query("SELECT COUNT(u) FROM AsyncExportJobDo u WHERE u.status = ?1")
    Integer countByStatus(AsyncJobStatusEnum status);

    @Query(value = "SELECT * FROM bo_bs_async_export_job u WHERE u.create_timestamp < ?1 ORDER BY create_timestamp ASC",
            nativeQuery = true)
    List<AsyncExportJobDo> findBeforeCreateTimestampWithOrderByCreateTimestampAsc(java.util.Date createTimestamp);
}