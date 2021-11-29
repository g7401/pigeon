package io.g740.pigeon.biz.persistence.jpa.account;

import io.g740.pigeon.biz.object.entity.account.UserDo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author bbottong
 */
public interface UserRepository extends PagingAndSortingRepository<UserDo, Long> {

    /**
     * 按username检查存在
     *
     * @param username
     * @return
     */
    boolean existsByUsername(String username);

    /**
     * 按username查找
     *
     * @param username
     * @return
     */
    UserDo findByUsername(String username);

    /**
     * 按username模糊搜索username
     *
     * @param username
     * @return
     */
    @Query("SELECT u.username FROM UserDo u WHERE u.username LIKE %?1%")
    List<String> fuzzyQueryByUsername(String username);
}
