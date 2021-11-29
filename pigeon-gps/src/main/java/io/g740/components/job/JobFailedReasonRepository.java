package io.g740.components.job;

import io.g740.components.job.JobFailedReasonDo;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author bbottong
 */
public interface JobFailedReasonRepository extends PagingAndSortingRepository<JobFailedReasonDo, Long> {

    /**
     * 按job category和job uid查找
     *
     * @param jobCategory
     * @param jobUid
     * @return
     */
    JobFailedReasonDo findByJobCategoryAndJobUid(String jobCategory, Long jobUid);
}
