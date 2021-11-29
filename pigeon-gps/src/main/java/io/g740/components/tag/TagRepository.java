package io.g740.components.tag;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author bbottong
 */
public interface TagRepository extends PagingAndSortingRepository<TagDo, Long> {

    /**
     * 按name查找
     *
     * @param name
     * @return
     */
    TagDo findByName(String name);

    /**
     * 按name检查存在
     *
     * @param name
     * @return
     */
    boolean existsByName(String name);
}
