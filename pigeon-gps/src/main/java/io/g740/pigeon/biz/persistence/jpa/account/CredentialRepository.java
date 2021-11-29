package io.g740.pigeon.biz.persistence.jpa.account;

import io.g740.pigeon.biz.object.entity.account.CredentialDo;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author bbottong
 */
public interface CredentialRepository extends PagingAndSortingRepository<CredentialDo, Long> {
    /**
     * 按username查找
     * 
     * @param username
     * @return
     */
    CredentialDo findByUsername(String username);
}
