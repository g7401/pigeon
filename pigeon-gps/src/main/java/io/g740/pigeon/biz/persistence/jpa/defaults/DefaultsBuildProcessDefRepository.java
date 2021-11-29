package io.g740.pigeon.biz.persistence.jpa.defaults;

import io.g740.pigeon.biz.object.entity.defaults.DefaultsBuildProcessDefDo;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author bbottong
 */
public interface DefaultsBuildProcessDefRepository extends PagingAndSortingRepository<DefaultsBuildProcessDefDo, Long> {
    /**
     * 按defaults category uid查找
     *
     * @param defaultsCategoryUid
     * @return
     */
    DefaultsBuildProcessDefDo findByDefaultsCategoryUid(Long defaultsCategoryUid);

    /**
     * 按enabled和schedule type查找
     *
     * @param enabled
     * @param scheduleType
     * @return
     */
    List<DefaultsBuildProcessDefDo> findByEnabledAndScheduleType(Boolean enabled, ScheduleTypeEnum scheduleType);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DefaultsBuildProcessDefDo findByUid(Long uid);

    /**
     * 按defaults category uid检查存在
     *
     * @param defaultsCategoryUid
     * @return
     */
    boolean existsByDefaultsCategoryUid(Long defaultsCategoryUid);
}
