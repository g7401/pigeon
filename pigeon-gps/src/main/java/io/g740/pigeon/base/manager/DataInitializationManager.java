package io.g740.pigeon.base.manager;

import io.g740.commons.constants.AsyncJobStatusEnum;
import io.g740.commons.constants.InfrastructureConstants;
import io.g740.commons.types.BaseDo;
import io.g740.components.job.JobHandler;

import io.g740.components.tag.TagDo;
import io.g740.components.tag.TagRepository;
import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.entity.export.AsyncExportJobConfDo;
import io.g740.pigeon.biz.object.entity.export.AsyncExportJobDo;
import io.g740.pigeon.biz.persistence.jpa.export.AsyncExportJobConfRepository;
import io.g740.pigeon.biz.persistence.jpa.export.AsyncExportJobRepository;
import io.g740.pigeon.biz.share.constants.TagConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;

/**
 * 初始化数据
 *
 * @author bbottong
 */
@Component
public class DataInitializationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializationManager.class);

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AsyncExportJobRepository asyncExportJobRepository;

    @Autowired
    private AsyncExportJobConfRepository asyncExportJobConfRepository;

    @Value("${application.df.export.pagination.page-size}")
    private Integer exportPageSize;

    @Value("${application.df.export.async-job.waiting-limit}")
    private Integer exportWaitingLimit;

    @Value("${application.df.export.async-job.running-limit}")
    private Integer exportRunningLimit;

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private JobHandler jobHandler;

    public void execute() throws Exception {
        initTag();
        initAsyncExportJobConf();
        clearExportJob();
    }

    /**
     * 初始化Tag
     */
    private void initTag() {
        TagDo tagDo = this.tagRepository.findByName(TagConstants.TAG_SYSTEM_NAME);
        if (tagDo == null) {
            tagDo = new TagDo();
            tagDo.setUid(TagConstants.TAG_SYSTEM_UID);
            tagDo.setName(TagConstants.TAG_SYSTEM_NAME);
            tagDo.setDescription(TagConstants.TAG_SYSTEM_DESCRIPTION);
            BaseDo.create(tagDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            this.tagRepository.save(tagDo);
        }
    }

    /**
     * 清理中断的导出任务
     */
    private void clearExportJob() {
        clearExportJob(AsyncJobStatusEnum.WAITING);
        clearExportJob(AsyncJobStatusEnum.RUNNING);
        clearExportJob(AsyncJobStatusEnum.SUSPENDED);
    }

    private void clearExportJob(AsyncJobStatusEnum asyncJobStatus) {
        PageRequest pageRequest;
        int pageNumber = 0;
        int pageSize = 10;
        Page<AsyncExportJobDo> pageOfAsyncExportJobDo;
        do {
            pageRequest = PageRequest.of(pageNumber++, pageSize);
            pageOfAsyncExportJobDo =
                    this.asyncExportJobRepository.findByStatus(asyncJobStatus, pageRequest);
            if (!pageOfAsyncExportJobDo.isEmpty()) {
                pageOfAsyncExportJobDo.getContent().forEach(asyncExportJobDo -> {
                    asyncExportJobDo.setStatus(AsyncJobStatusEnum.FAILED);
                    asyncExportJobDo.setFailedTimestamp(new Date());
                    BaseDo.update(asyncExportJobDo, InfrastructureConstants.ROOT_USERNAME, new Date());

                    this.jobHandler.saveJobFailedReason(AsyncExportJobDo.RESOURCE_NAME,
                            asyncExportJobDo.getUid(),
                            "origin status: " + asyncJobStatus + ", clear waiting/running/suspended export job at " +
                                    "init");
                });
                this.asyncExportJobRepository.saveAll(pageOfAsyncExportJobDo.getContent());
            }
        } while (pageOfAsyncExportJobDo != null && !pageOfAsyncExportJobDo.isEmpty());
    }

    private void initAsyncExportJobConf() {
        Iterator<AsyncExportJobConfDo> iterator =
                this.asyncExportJobConfRepository.findAll().iterator();
        if (iterator.hasNext()) {
            return;
        } else {
            AsyncExportJobConfDo asyncExportJobConfDo = new AsyncExportJobConfDo();
            asyncExportJobConfDo.setMaxWaitingThreshold(this.exportWaitingLimit);
            asyncExportJobConfDo.setMaxRunningThreshold(this.exportRunningLimit);
            asyncExportJobConfDo.setPageSize(this.exportPageSize);
            BaseDo.create(asyncExportJobConfDo, InfrastructureConstants.ROOT_USERNAME, new java.util.Date());
            this.asyncExportJobConfRepository.save(asyncExportJobConfDo);
        }

    }
}
