package io.g740.pigeon.base.persistence.jpa;

import io.g740.pigeon.base.object.entity.SystemReleaseDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author bbottong
 */
public interface SystemReleaseRepository extends PagingAndSortingRepository<SystemReleaseDo, Long> {
    /**
     * 分页查找，要求enabled=true，且按create timestamp倒序排列结果
     *
     * @param pageable
     * @return
     */
    @Query("SELECT u FROM SystemReleaseDo u WHERE u.enabled = 1 ORDER BY u.createTimestamp DESC")
    Page<SystemReleaseDo> findEnabledAndOrderByCreateTimestampDesc(Pageable pageable);

    /**
     * 查找，要求enabled=true
     *
     * @return
     */
    @Query("SELECT u FROM SystemReleaseDo u WHERE u.enabled = 1")
    List<SystemReleaseDo> findEnabled();

}
