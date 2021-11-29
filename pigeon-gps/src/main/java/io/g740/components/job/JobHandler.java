package io.g740.components.job;

import io.g740.commons.constants.InfrastructureConstants;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;

import io.g740.components.uid.tinyid.IdHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author bbottong
 */
@Handler
public class JobHandler {
    @Autowired
    private JobFailedReasonRepository jobFailedReasonRepository;

    @Autowired
    private IdHelper idHelper;

    public void saveJobFailedReason(String jobCategory, Long jobUid, String content) {
        JobFailedReasonDo jobFailedReasonDo = new JobFailedReasonDo();
        jobFailedReasonDo.setUid(this.idHelper.getNextId(JobFailedReasonDo.RESOURCE_NAME));
        jobFailedReasonDo.setJobCategory(jobCategory);
        jobFailedReasonDo.setJobUid(jobUid);
        if (content.length() > JobFailedReasonDo.MAX_LENGTH) {
            jobFailedReasonDo.setContent(content.substring(0, JobFailedReasonDo.MAX_LENGTH));
        } else {
            jobFailedReasonDo.setContent(content);
        }
        BaseDo.create(jobFailedReasonDo, InfrastructureConstants.ROOT_USERNAME, new Date());

        this.jobFailedReasonRepository.save(jobFailedReasonDo);
    }

    public JobFailedReasonDto getJobFailedReason(String jobCategory, Long jobUid) throws ServiceException {
        JobFailedReasonDo jobFailedReasonDo = this.jobFailedReasonRepository.findByJobCategoryAndJobUid(jobCategory,
                jobUid);
        if (jobFailedReasonDo != null) {
            JobFailedReasonDto jobFailedReasonDto = new JobFailedReasonDto();
            BeanUtils.copyProperties(jobFailedReasonDo, jobFailedReasonDto);
            return jobFailedReasonDto;
        }

        return null;
    }
}
