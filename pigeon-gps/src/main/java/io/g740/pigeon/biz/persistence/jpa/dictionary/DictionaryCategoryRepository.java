package io.g740.pigeon.biz.persistence.jpa.dictionary;

import io.g740.pigeon.biz.object.entity.dictionary.DictionaryCategoryDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bbottong
 */
@Repository
public interface DictionaryCategoryRepository extends PagingAndSortingRepository<DictionaryCategoryDo, Long> {
    /**
     * 按name检查是否存在dictionary category
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
    DictionaryCategoryDo findByUid(Long uid);

    /**
     * 按uid检查是否存在dictionary category
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
    @Query("DELETE FROM DictionaryCategoryDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);


}
