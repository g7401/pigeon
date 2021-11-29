package io.g740.pigeon.biz.persistence.jpa.app;

import io.g740.pigeon.biz.object.entity.app.AppUserDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface AppUserRepository extends PagingAndSortingRepository<AppUserDo, Long> {
    /**
     * 按app uid检查存在
     *
     * @param appUid
     * @return
     */
    boolean existsByAppUid(Long appUid);

    /**
     * 按app uid查找username
     *
     * @param appUid
     * @return
     */
    @Query("SELECT u.username FROM AppUserDo u WHERE u.appUid=?1")
    List<String> findByAppUid(Long appUid);

    /**
     * 按app uid删除
     *
     * @param appUid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM AppUserDo u WHERE u.appUid = ?1")
    void deleteByAppUid(Long appUid);

    /**
     * 按username查找app
     *
     * @param username
     * @return
     */
    @Query("SELECT u.appUid FROM AppUserDo u WHERE u.username=?1")
    List<Long> findByUsername(String username);
}
