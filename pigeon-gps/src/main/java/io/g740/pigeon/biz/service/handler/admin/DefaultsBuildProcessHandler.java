package io.g740.pigeon.biz.service.handler.admin;

import com.google.common.base.Strings;
import io.g740.commons.constants.AsyncJobStatusEnum;
import io.g740.commons.constants.InfrastructureConstants;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;

import io.g740.components.dlock.Dlock;
import io.g740.components.dlock.DlockService;
import io.g740.components.uid.SingleInstanceUidGenerator;
import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.defaults.DefaultsBuildProcessInstDto;
import io.g740.pigeon.biz.object.dto.defaults.TestDefaultsBuildProcessDefDto;
import io.g740.pigeon.biz.object.entity.defaults.DefaultsBuildProcessDefDo;
import io.g740.pigeon.biz.object.entity.defaults.DefaultsBuildProcessInstDo;
import io.g740.pigeon.biz.object.entity.defaults.DefaultsContentDo;
import io.g740.pigeon.biz.persistence.jpa.defaults.DefaultsBuildProcessDefRepository;
import io.g740.pigeon.biz.persistence.jpa.defaults.DefaultsBuildProcessInstRepository;
import io.g740.pigeon.biz.persistence.jpa.defaults.DefaultsContentRepository;
import io.g740.components.job.JobHandler;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author bbottong
 */
@Handler
public class DefaultsBuildProcessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultsBuildProcessHandler.class);

    @Autowired
    private DefaultsBuildProcessDefRepository defaultsBuildProcessDefRepository;

    @Autowired
    private SqlBuildStrategyHandler sqlBuildStrategyHandler;

    @Autowired
    private DefaultsBuildProcessInstRepository defaultsBuildProcessInstRepository;

    @Autowired
    private DefaultsContentRepository defaultsContentRepository;

    @Autowired
    private JobHandler jobHandler;

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private SingleInstanceUidGenerator singleInstanceUidGenerator;

    @Autowired
    private TaskScheduler taskScheduler;

    private Map<Long, ScheduledFuture<?>> processDefUidAndScheduledFuture = new ConcurrentHashMap<>();

    private static final String DEFAULTS_BUILD_PROCESS_SCHEDULER_DLOCK_NAME = "defaults_build_process_scheduler";

    @Autowired
    private DlockService dlockService;

    public SimpleTreeNode testDefaultsBuildProcess(
            TestDefaultsBuildProcessDefDto testDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        switch (testDefaultsBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                try {
                    SimpleTreeNode contentTreeNode = this.sqlBuildStrategyHandler.testBuildStrategy(
                            testDefaultsBuildProcessDefDto.getSqlBuildStrategyContent());

                    return contentTreeNode;
                } catch (Exception e) {
                    throw new IllegalParameterException(e.getMessage());
                }
            default:
                throw new IllegalParameterException("unsupported defaults build strategy type");
        }
    }

    public PageResult<DefaultsBuildProcessInstDto> queryDefaultsBuildProcessInst(
            Long processDefUid,
            Map<String, String[]> parameterMap,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        Page<DefaultsBuildProcessInstDo> pageResponse;

        // 如果没有指定排序，则设定默认排序
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            pageResponse = this.defaultsBuildProcessInstRepository.findByProcessDefUid(processDefUid, pageable);
        } else {
            // 指定排序字段
            Sort sort = Sort.by(Sort.Order.desc("id"));
            PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(),
                    pageable.getPageSize(), sort);
            pageResponse = this.defaultsBuildProcessInstRepository.findByProcessDefUid(processDefUid, pageRequest);
        }

        //
        PageResult<DefaultsBuildProcessInstDto> pageResult = new PageResult<>();
        pageResult.setContent(new ArrayList<>());
        for (DefaultsBuildProcessInstDo item : pageResponse.getContent()) {
            DefaultsBuildProcessInstDto defaultsBuildProcessInstDto = new DefaultsBuildProcessInstDto();
            defaultsBuildProcessInstDto.setProcessDefUid(item.getProcessDefUid());
            defaultsBuildProcessInstDto.setUid(item.getUid());
            defaultsBuildProcessInstDto.setStatus(item.getStatus());
            defaultsBuildProcessInstDto.setStartTimestamp(item.getStartTimestamp());
            defaultsBuildProcessInstDto.setDoneTimestamp(item.getDoneTimestamp());
            defaultsBuildProcessInstDto.setFailedTimestamp(item.getFailedTimestamp());
            defaultsBuildProcessInstDto.setCanceledTimestamp(item.getCanceledTimestamp());
            defaultsBuildProcessInstDto.setSuspendedTimestamp(item.getSuspendedTimestamp());

            pageResult.getContent().add(defaultsBuildProcessInstDto);
        }
        pageResult.setNumberOfElements(pageResponse.getNumberOfElements());
        pageResult.setPageNumber(pageResponse.getNumber());
        pageResult.setPageSize(pageResponse.getSize());
        pageResult.setTotalElements(pageResponse.getTotalElements());
        pageResult.setTotalPages(pageResponse.getTotalPages());

        return pageResult;
    }

    /**
     * 初始化批量登记调度
     *
     * @throws Exception
     */
    @Dlock(bizType = DEFAULTS_BUILD_PROCESS_SCHEDULER_DLOCK_NAME, timeoutInMinutes = 60)
    @Transactional(rollbackFor = Exception.class)
    public void initRegisterScheduling() throws Exception {
        String dlockSerialNo = this.dlockService.getLock(DEFAULTS_BUILD_PROCESS_SCHEDULER_DLOCK_NAME);

        try {
            List<DefaultsBuildProcessDefDo> listOfDefaultsBuildProcessDefDo =
                    this.defaultsBuildProcessDefRepository.findByEnabledAndScheduleType(Boolean.TRUE,
                            ScheduleTypeEnum.PERIODIC);
            if (CollectionUtils.isEmpty(listOfDefaultsBuildProcessDefDo)) {
                return;
            }

            for (DefaultsBuildProcessDefDo item : listOfDefaultsBuildProcessDefDo) {
                initRegisterScheduling(item.getUid(), item.getScheduleTypeExtDetails());
            }
        } finally {
            this.dlockService.releaseLock(DEFAULTS_BUILD_PROCESS_SCHEDULER_DLOCK_NAME, dlockSerialNo);
        }
    }

    /**
     * 登记调度
     *
     * @param processDefUid
     * @param cronExpression
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void initRegisterScheduling(Long processDefUid, String cronExpression) throws Exception {
        ScheduledFuture<?> future = this.taskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Start to execute defaults build for process def " + processDefUid);
                try {
                    executeDefaultsBuild(processDefUid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LOGGER.info("Done to execute defaults build for process def " + processDefUid);
            }
        }, new CronTrigger(cronExpression));

        this.processDefUidAndScheduledFuture.put(processDefUid, future);
    }

    /**
     * 注销调度
     *
     * @param processDefUid
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeScheduling(Long processDefUid) {
        ScheduledFuture<?> future = this.processDefUidAndScheduledFuture.get(processDefUid);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 执行构建流程
     *
     * @param processDefUid
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    private void executeDefaultsBuild(Long processDefUid) throws Exception {
        DefaultsBuildProcessDefDo defaultsBuildProcessDefDo =
                this.defaultsBuildProcessDefRepository.findByUid(processDefUid);
        if (defaultsBuildProcessDefDo == null) {
            throw new Exception("cannot find defaults build process def " + processDefUid);
        }

        if (!Boolean.TRUE.equals(defaultsBuildProcessDefDo.getEnabled())) {
            revokeScheduling(processDefUid);
        }

        // 记录process inst启动
        DefaultsBuildProcessInstDo defaultsBuildProcessInstDo = new DefaultsBuildProcessInstDo();
        defaultsBuildProcessInstDo.setUid(this.idHelper.getNextId(DefaultsBuildProcessInstDo.RESOURCE_NAME));
        defaultsBuildProcessInstDo.setProcessDefUid(processDefUid);
        defaultsBuildProcessInstDo.setStatus(AsyncJobStatusEnum.RUNNING);
        defaultsBuildProcessInstDo.setStartTimestamp(new Date());
        BaseDo.create(defaultsBuildProcessInstDo, InfrastructureConstants.ROOT_USERNAME, new Date());
        this.defaultsBuildProcessInstRepository.save(defaultsBuildProcessInstDo);

        try {
            switch (defaultsBuildProcessDefDo.getBuildStrategyType()) {
                case SQL: {
                    SimpleTreeNode treeNode =
                            this.sqlBuildStrategyHandler.executeBuildStrategy(
                                    defaultsBuildProcessDefDo.getBuildStrategyContent());
                    replaceDefaultsContent(defaultsBuildProcessDefDo.getDefaultsCategoryUid(), treeNode);
                    break;
                }
                default:
                    break;
            }

            // 记录process inst完成
            defaultsBuildProcessInstDo.setDoneTimestamp(new Date());
            defaultsBuildProcessInstDo.setStatus(AsyncJobStatusEnum.DONE);
            BaseDo.update(defaultsBuildProcessInstDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            this.defaultsBuildProcessInstRepository.save(defaultsBuildProcessInstDo);
        } catch (Exception e) {
            // 记录process inst失败
            defaultsBuildProcessInstDo.setFailedTimestamp(new Date());
            defaultsBuildProcessInstDo.setStatus(AsyncJobStatusEnum.FAILED);
            BaseDo.update(defaultsBuildProcessInstDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            this.defaultsBuildProcessInstRepository.save(defaultsBuildProcessInstDo);

            // 记录process inst失败原因
            String errorMessage = "failed to execute defaults build process def uid " + processDefUid + ". More" +
                    " info: " + e.getMessage();
            this.jobHandler.saveJobFailedReason(DefaultsBuildProcessDefDo.RESOURCE_NAME,
                    defaultsBuildProcessInstDo.getUid(),
                    errorMessage);

            throw new Exception(errorMessage, e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private void replaceDefaultsContent(Long defaultsCategoryUid, SimpleTreeNode contentTreeNode) throws Exception {
        // Step 1, 清除指定默认值类目的现有的默认值内容，只删除自动创建，不删除人工创建的
        this.defaultsContentRepository.deleteByDefaultsCategoryUidAndCreateBy(
                defaultsCategoryUid, InfrastructureConstants.ROOT_USERNAME);

        // Step 2, 为指定默认值类目插入新的默认值内容
        Date now = new Date();
        if (CollectionUtils.isNotEmpty(contentTreeNode.getChildren())) {
            List<DefaultsContentDo> listOfDefaultsContentDo = new ArrayList<>();

            contentTreeNode.getChildren().forEach(treeNode -> {
                DefaultsContentDo defaultsContentDo = new DefaultsContentDo();
                defaultsContentDo.setDefaultsCategoryUid(defaultsCategoryUid);
                defaultsContentDo.setUid(this.singleInstanceUidGenerator.generateUid(
                        DefaultsContentDo.RESOURCE_NAME));
                defaultsContentDo.setValue(treeNode.getName());
                if (Strings.isNullOrEmpty(treeNode.getDescription())) {
                    defaultsContentDo.setLabel(treeNode.getName());
                } else {
                    defaultsContentDo.setLabel(treeNode.getDescription());
                }
                BaseDo.create(defaultsContentDo, InfrastructureConstants.ROOT_USERNAME, now);

                listOfDefaultsContentDo.add(defaultsContentDo);
            });

            this.defaultsContentRepository.saveAll(listOfDefaultsContentDo);
        }
    }

}
