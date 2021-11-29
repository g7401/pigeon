package io.g740.pigeon.biz.persistence.jpa.defaults;

import io.g740.pigeon.biz.object.entity.defaults.DefaultsBuildProcessInstDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author bbottong
 */
public interface DefaultsBuildProcessInstRepository extends PagingAndSortingRepository<DefaultsBuildProcessInstDo, Long> {

    /**
     * 按process def uid分页查找
     *
     * @param processDefUid
     * @param pageable
     * @return
     */
    Page<DefaultsBuildProcessInstDo> findByProcessDefUid(Long processDefUid, Pageable pageable);
}
