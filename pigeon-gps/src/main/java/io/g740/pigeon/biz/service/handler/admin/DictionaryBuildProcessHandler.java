package io.g740.pigeon.biz.service.handler.admin;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
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
import io.g740.pigeon.biz.object.dto.dictionary.DictionaryBuildProcessInstDto;
import io.g740.pigeon.biz.object.dto.dictionary.TestDictionaryBuildProcessDefDto;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryBuildProcessDefDo;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryBuildProcessInstDo;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryContentDo;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryStructureDo;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryBuildProcessDefRepository;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryBuildProcessInstRepository;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryContentRepository;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryStructureRepository;
import io.g740.components.job.JobHandler;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
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
public class DictionaryBuildProcessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryBuildProcessHandler.class);

    @Autowired
    private DictionaryBuildProcessDefRepository dictionaryBuildProcessDefRepository;

    @Autowired
    private SqlBuildStrategyHandler sqlBuildStrategyHandler;

    @Autowired
    private DictionaryBuildProcessInstRepository dictionaryBuildProcessInstRepository;

    @Autowired
    private DictionaryContentRepository dictionaryContentRepository;

    @Autowired
    private DictionaryStructureRepository dictionaryStructureRepository;

    @Autowired
    private JobHandler jobHandler;

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private SingleInstanceUidGenerator singleInstanceUidGenerator;

    @Autowired
    private TaskScheduler taskScheduler;

    private Map<Long, ScheduledFuture<?>> processDefUidAndScheduledFuture = new ConcurrentHashMap<>();

    private static final String DICTIONARY_BUILD_PROCESS_SCHEDULER_DLOCK_NAME = "dictionary_build_process_scheduler";

    @Autowired
    private DlockService dlockService;

    public SimpleTreeNode testDictionaryBuild(
            TestDictionaryBuildProcessDefDto testDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        switch (testDictionaryBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                try {
                    return this.sqlBuildStrategyHandler.testBuildStrategy(
                            testDictionaryBuildProcessDefDto.getSqlBuildStrategyContent());
                } catch (Exception e) {
                    throw new IllegalParameterException(e.getMessage());
                }
            default:
                throw new IllegalParameterException("unsupported dictionary build strategy type");
        }
    }

    public PageResult<DictionaryBuildProcessInstDto> queryDictionaryBuildProcessInst(
            Long processDefUid,
            Map<String, String[]> parameterMap,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        Page<DictionaryBuildProcessInstDo> pageResponse;

        // ????????????????????????????????????????????????
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            pageResponse = this.dictionaryBuildProcessInstRepository.findByProcessDefUid(processDefUid, pageable);
        } else {
            // ??????????????????
            // ????????????Sort?????????????????????????????????
            Sort sort = Sort.by(Sort.Order.desc("id"));
            PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(),
                    pageable.getPageSize(), sort);
            pageResponse = this.dictionaryBuildProcessInstRepository.findByProcessDefUid(processDefUid, pageRequest);
        }

        //
        PageResult<DictionaryBuildProcessInstDto> pageResult = new PageResult<>();
        pageResult.setContent(new ArrayList<>());
        for (DictionaryBuildProcessInstDo item : pageResponse.getContent()) {
            DictionaryBuildProcessInstDto dictionaryBuildProcessInstDto = new DictionaryBuildProcessInstDto();
            dictionaryBuildProcessInstDto.setProcessDefUid(item.getProcessDefUid());
            dictionaryBuildProcessInstDto.setUid(item.getUid());
            dictionaryBuildProcessInstDto.setStatus(item.getStatus());
            dictionaryBuildProcessInstDto.setStartTimestamp(item.getStartTimestamp());
            dictionaryBuildProcessInstDto.setDoneTimestamp(item.getDoneTimestamp());
            dictionaryBuildProcessInstDto.setFailedTimestamp(item.getFailedTimestamp());
            dictionaryBuildProcessInstDto.setCanceledTimestamp(item.getCanceledTimestamp());
            dictionaryBuildProcessInstDto.setSuspendedTimestamp(item.getSuspendedTimestamp());

            pageResult.getContent().add(dictionaryBuildProcessInstDto);
        }
        pageResult.setNumberOfElements(pageResponse.getNumberOfElements());
        pageResult.setPageNumber(pageResponse.getNumber());
        pageResult.setPageSize(pageResponse.getSize());
        pageResult.setTotalElements(pageResponse.getTotalElements());
        pageResult.setTotalPages(pageResponse.getTotalPages());

        return pageResult;
    }

    /**
     * ???????????????????????????
     *
     * @throws Exception
     */
    @Dlock(bizType = DICTIONARY_BUILD_PROCESS_SCHEDULER_DLOCK_NAME, timeoutInMinutes = 60)
    @Transactional(rollbackFor = Exception.class)
    public void initRegisterScheduling() throws Exception {
        String dlockSerialNo = this.dlockService.getLock(DICTIONARY_BUILD_PROCESS_SCHEDULER_DLOCK_NAME);

        try {
            List<DictionaryBuildProcessDefDo> listOfDictionaryBuildProcessDefDo =
                    this.dictionaryBuildProcessDefRepository.findByEnabledAndScheduleType(Boolean.TRUE, ScheduleTypeEnum.PERIODIC);
            if (CollectionUtils.isEmpty(listOfDictionaryBuildProcessDefDo)) {
                return;
            }

            for (DictionaryBuildProcessDefDo item : listOfDictionaryBuildProcessDefDo) {
                initRegisterScheduling(item.getUid(), item.getScheduleTypeExtDetails());
            }
        } finally {
            this.dlockService.releaseLock(DICTIONARY_BUILD_PROCESS_SCHEDULER_DLOCK_NAME, dlockSerialNo);
        }
    }

    /**
     * ????????????
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
                LOGGER.info("Start to execute dictionary build for process def " + processDefUid);
                try {
                    executeDictionaryBuild(processDefUid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LOGGER.info("Done to execute dictionary build for process def " + processDefUid);
            }
        }, new CronTrigger(cronExpression));

        this.processDefUidAndScheduledFuture.put(processDefUid, future);
    }

    /**
     * ????????????
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
     * ??????????????????
     *
     * @param processDefUid
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    private void executeDictionaryBuild(Long processDefUid) throws Exception {
        DictionaryBuildProcessDefDo dictionaryBuildProcessDefDo =
                this.dictionaryBuildProcessDefRepository.findByUid(processDefUid);
        if (dictionaryBuildProcessDefDo == null) {
            throw new Exception("cannot find dictionary build process def " + processDefUid);
        }

        if (!Boolean.TRUE.equals(dictionaryBuildProcessDefDo.getEnabled())) {
            revokeScheduling(processDefUid);
        }

        // ??????process inst??????
        DictionaryBuildProcessInstDo dictionaryBuildProcessInstDo = new DictionaryBuildProcessInstDo();
        dictionaryBuildProcessInstDo.setUid(this.idHelper.getNextId(DictionaryBuildProcessInstDo.RESOURCE_NAME));
        dictionaryBuildProcessInstDo.setProcessDefUid(processDefUid);
        dictionaryBuildProcessInstDo.setStatus(AsyncJobStatusEnum.RUNNING);
        dictionaryBuildProcessInstDo.setStartTimestamp(new Date());
        BaseDo.create(dictionaryBuildProcessInstDo, InfrastructureConstants.ROOT_USERNAME, new Date());
        this.dictionaryBuildProcessInstRepository.save(dictionaryBuildProcessInstDo);

        try {
            switch (dictionaryBuildProcessDefDo.getBuildStrategyType()) {
                case SQL: {
                    SimpleTreeNode treeNode =
                            this.sqlBuildStrategyHandler.executeBuildStrategy(
                                    dictionaryBuildProcessDefDo.getBuildStrategyContent());
                    replaceDictionaryContent(dictionaryBuildProcessDefDo.getDictionaryCategoryUid(), treeNode);
                    break;
                }
                default:
                    break;
            }

            // ??????process inst??????
            dictionaryBuildProcessInstDo.setDoneTimestamp(new Date());
            dictionaryBuildProcessInstDo.setStatus(AsyncJobStatusEnum.DONE);
            BaseDo.update(dictionaryBuildProcessInstDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            this.dictionaryBuildProcessInstRepository.save(dictionaryBuildProcessInstDo);
        } catch (Exception e) {
            // ??????process inst??????
            dictionaryBuildProcessInstDo.setFailedTimestamp(new Date());
            dictionaryBuildProcessInstDo.setStatus(AsyncJobStatusEnum.FAILED);
            BaseDo.update(dictionaryBuildProcessInstDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            this.dictionaryBuildProcessInstRepository.save(dictionaryBuildProcessInstDo);

            // ??????process inst????????????
            String errorMessage = "failed to execute dictionary build process def uid " + processDefUid + ". More" +
                    " info: " + e.getMessage();
            this.jobHandler.saveJobFailedReason(DictionaryBuildProcessDefDo.RESOURCE_NAME,
                    dictionaryBuildProcessInstDo.getUid(),
                    errorMessage);

            throw new Exception(errorMessage, e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private void replaceDictionaryContent(Long dictionaryCategoryUid, SimpleTreeNode contentTreeNode) throws Exception {
        // Step 1, ???????????????????????????????????????
        SimpleTreeNode structureTreeNode = buildDictionaryStructureTree(dictionaryCategoryUid);

        // Step 2, ???????????????????????????????????????????????????????????????????????????????????????????????????
        this.dictionaryContentRepository.deleteByDictionaryCategoryUidAndCreateBy(
                dictionaryCategoryUid, InfrastructureConstants.ROOT_USERNAME);

        // Step 3, ?????????????????????????????????????????????
        List<DictionaryContentDo> listOfDictionaryContentDo = new LinkedList<>();
        recursiveHandleTreeNode(null, contentTreeNode, structureTreeNode, new Date(), dictionaryCategoryUid, listOfDictionaryContentDo);
        this.dictionaryContentRepository.saveAll(listOfDictionaryContentDo);
    }

    private SimpleTreeNode buildDictionaryStructureTree(Long dictionaryCategoryUid) {
        SimpleTreeNode rootTreeNode = SimpleTreeNode.buildRootTreeNode();

        List<DictionaryStructureDo> listOfDictionaryStructureDo =
                this.dictionaryStructureRepository.findByDictionaryCategoryUid(dictionaryCategoryUid);
        if (CollectionUtils.isEmpty(listOfDictionaryStructureDo)) {
            return rootTreeNode;
        }

        rootTreeNode.setChildren(new ArrayList<>());

        // ????????????????????????????????????????????????????????????key - dictionary structure uid
        Map<Long, SimpleTreeNode> mapOfDictionaryStructureTreeNode = new HashMap<>(10);

        for (DictionaryStructureDo dictionaryStructureDo : listOfDictionaryStructureDo) {
            //
            // Step 1?????????????????????????????????
            //
            SimpleTreeNode dictionaryStructureTreeNode;
            if (mapOfDictionaryStructureTreeNode.containsKey(dictionaryStructureDo.getUid())) {
                // ????????????????????????????????????????????????children???????????????????????????????????????????????????????????????
                // ??????????????????????????????????????????children?????????
                dictionaryStructureTreeNode =
                        mapOfDictionaryStructureTreeNode.get(dictionaryStructureDo.getUid());
                dictionaryStructureTreeNode.setName(dictionaryStructureDo.getName());
                dictionaryStructureTreeNode.setDescription(dictionaryStructureDo.getDescription());
                dictionaryStructureTreeNode.setType(null);
            } else {
                dictionaryStructureTreeNode = new SimpleTreeNode();
                dictionaryStructureTreeNode.setUidCode(dictionaryStructureDo.getUid());
                dictionaryStructureTreeNode.setName(dictionaryStructureDo.getName());
                dictionaryStructureTreeNode.setDescription(dictionaryStructureDo.getDescription());
                dictionaryStructureTreeNode.setType(null);
                dictionaryStructureTreeNode.setChildren(null);

                mapOfDictionaryStructureTreeNode.put(dictionaryStructureTreeNode.getUidCode(),
                        dictionaryStructureTreeNode);
            }

            //
            // Step 2, ??????????????????????????????????????????
            //
            if (dictionaryStructureDo.getParentUid() == null) {
                rootTreeNode.getChildren().add(dictionaryStructureTreeNode);
            } else {
                if (mapOfDictionaryStructureTreeNode.containsKey(dictionaryStructureDo.getParentUid())) {
                    // ??????????????????????????????
                    SimpleTreeNode parentDictionaryStructureTreeNode =
                            mapOfDictionaryStructureTreeNode.get(dictionaryStructureDo.getParentUid());
                    if (org.apache.commons.collections4.CollectionUtils.isEmpty(parentDictionaryStructureTreeNode.getChildren())) {
                        parentDictionaryStructureTreeNode.setChildren(new ArrayList<>());
                    }
                    parentDictionaryStructureTreeNode.getChildren().add(dictionaryStructureTreeNode);
                } else {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    SimpleTreeNode shadowDictionaryStructureTreeNode = new SimpleTreeNode();
                    shadowDictionaryStructureTreeNode.setUidCode(dictionaryStructureDo.getParentUid());
                    shadowDictionaryStructureTreeNode.setName(null);
                    shadowDictionaryStructureTreeNode.setDescription(null);
                    shadowDictionaryStructureTreeNode.setType(null);

                    mapOfDictionaryStructureTreeNode.put(shadowDictionaryStructureTreeNode.getUidCode(),
                            shadowDictionaryStructureTreeNode);

                    // ????????????????????????????????????
                    shadowDictionaryStructureTreeNode.setChildren(new ArrayList<>());
                    shadowDictionaryStructureTreeNode.getChildren().add(dictionaryStructureTreeNode);
                }
            }
        }

        return rootTreeNode;
    }

    @Transactional(rollbackFor = Exception.class)
    private void recursiveHandleTreeNode(
            Long parentUid,
            SimpleTreeNode contentTreeNode,
            SimpleTreeNode structureTreeNode,
            Date timestamp,
            Long dictionaryCategoryUid,
            List<DictionaryContentDo> listOfDictionaryContentDo) {
        if (!contentTreeNode.getUidCode().equals(SimpleTreeNode.ROOT_UID)) {
            // ???????????????ROOT????????????????????????
            DictionaryContentDo dictionaryContentDo = new DictionaryContentDo();
            dictionaryContentDo.setUid(this.singleInstanceUidGenerator.generateUid(DictionaryContentDo.RESOURCE_NAME));
            dictionaryContentDo.setValue(contentTreeNode.getName());
            if (Strings.isNullOrEmpty(contentTreeNode.getDescription())) {
                dictionaryContentDo.setLabel(contentTreeNode.getName());
            } else {
                dictionaryContentDo.setLabel(contentTreeNode.getDescription());
            }
            dictionaryContentDo.setDictionaryCategoryUid(dictionaryCategoryUid);
            dictionaryContentDo.setDictionaryStructureUid(structureTreeNode.getUidCode());
            dictionaryContentDo.setParentUid(parentUid);
            BaseDo.create(dictionaryContentDo, InfrastructureConstants.ROOT_USERNAME, timestamp);

            // ?????????????????????dictionary content?????????save all
            listOfDictionaryContentDo.add(dictionaryContentDo);

            // ??????parent uid
            parentUid = dictionaryContentDo.getUid();
        }

        if (CollectionUtils.isEmpty(contentTreeNode.getChildren())) {
            return;
        }

        for (SimpleTreeNode childContentTreeNode : contentTreeNode.getChildren()) {
            recursiveHandleTreeNode(parentUid,
                    childContentTreeNode,
                    structureTreeNode.getChildren().get(0),
                    timestamp,
                    dictionaryCategoryUid,
                    listOfDictionaryContentDo);
        }
    }
}
