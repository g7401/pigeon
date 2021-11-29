package io.g740.pigeon.biz.persistence.jpa.dictionary;

import io.g740.pigeon.biz.object.entity.dictionary.DictionaryBuildProcessInstDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author bbottong
 */
public interface DictionaryBuildProcessInstRepository extends PagingAndSortingRepository<DictionaryBuildProcessInstDo, Long> {

    /**
     * 按process def uid分页查找
     *
     * @param processDefUid
     * @param pageable
     * @return
     */
    Page<DictionaryBuildProcessInstDo> findByProcessDefUid(Long processDefUid, Pageable pageable);
}
