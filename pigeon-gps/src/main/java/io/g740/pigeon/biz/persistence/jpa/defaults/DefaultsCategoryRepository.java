package io.g740.pigeon.biz.persistence.jpa.defaults;

import io.g740.pigeon.biz.object.entity.defaults.DefaultsCategoryDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
@Repository
public interface DefaultsCategoryRepository extends PagingAndSortingRepository<DefaultsCategoryDo, Long> {
    /**
     * 按name检查是否存在defaults category
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
    DefaultsCategoryDo findByUid(Long uid);

    /**
     * 按uid检查是否存在defaults category
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
    @Query("DELETE FROM DefaultsCategoryDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);

    /**
     * 按名称清单分页查找
     *
     * @param names
     * @param pageable
     * @return
     */
    Page<DefaultsCategoryDo> findByNameIn(List<String> names, Pageable pageable);
}
