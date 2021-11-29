package io.g740.pigeon.biz.persistence.jpa.export;

import io.g740.pigeon.biz.object.entity.export.AsyncExportJobConfDo;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author bbottong
 */
public interface AsyncExportJobConfRepository extends PagingAndSortingRepository<AsyncExportJobConfDo, Long> {
}