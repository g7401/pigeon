package io.g740.pigeon.biz.persistence.jpa.dictionary;

import io.g740.pigeon.biz.object.entity.dictionary.DictionaryBuildProcessDefDo;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author bbottong
 */
public interface DictionaryBuildProcessDefRepository extends PagingAndSortingRepository<DictionaryBuildProcessDefDo, Long> {
    /**
     * 按dictionary category uid查找
     *
     * @param dictionaryCategoryUid
     * @return
     */
    DictionaryBuildProcessDefDo findByDictionaryCategoryUid(Long dictionaryCategoryUid);

    /**
     * 按enabled和schedule type查找
     *
     * @param enabled
     * @param scheduleType
     * @return
     */
    List<DictionaryBuildProcessDefDo> findByEnabledAndScheduleType(Boolean enabled, ScheduleTypeEnum scheduleType);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DictionaryBuildProcessDefDo findByUid(Long uid);


    /**
     * 按dictionary category uid检查
     *
     * @param dictionaryCategoryUid
     * @return
     */
    boolean existsByDictionaryCategoryUid(Long dictionaryCategoryUid);
}
