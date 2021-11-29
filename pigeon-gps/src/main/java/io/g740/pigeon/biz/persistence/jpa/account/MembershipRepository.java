package io.g740.pigeon.biz.persistence.jpa.account;

import io.g740.pigeon.biz.object.entity.account.MembershipDo;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author bbottong
 */
public interface MembershipRepository extends PagingAndSortingRepository<MembershipDo, Long> {
    /**
     * 按username查找
     *
     * @param username
     * @return
     */
    MembershipDo findByUsername(String username);
}
