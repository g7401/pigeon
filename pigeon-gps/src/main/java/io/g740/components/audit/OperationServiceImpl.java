package io.g740.components.audit;

import com.google.common.base.Strings;
import io.g740.commons.constants.InfrastructureConstants;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.PageResult;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class OperationServiceImpl implements OperationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationServiceImpl.class);

    private final BlockingQueue<OperationDo> operationBlockingQueue = new LinkedBlockingQueue<>(400);

    private final BlockingQueue<OperationTaskDo> operationTaskBlockingQueue = new LinkedBlockingQueue<>(400);

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private OperationTaskRepository operationTaskRepository;

    @Value("${application.audit.delete-operation-exists-days}")
    private Integer auditDeleteOperationExistsDays;

    public static final String XXL_JOB_NAME_OPERATION_SAVE_OPERATION_HANDLER =
            "operation.save_operation_handler";
    public static final String XXL_JOB_CRON_OPERATION_SAVE_OPERATION_HANDLER = "0/3 * * * * ?";

    public static final String XXL_JOB_NAME_OPERATION_SAVE_OPERATION_TASK_HANDLER =
            "operation.save_operation_task_handler";
    public static final String XXL_JOB_CRON_OPERATION_SAVE_OPERATION_TASK_HANDLER = "0/5 * * * * ?";

    public static final String XXL_JOB_NAME_OPERATION_CLEANUP_OPERATION_HANDLER =
            "operation.cleanup_operation_handler";
    public static final String XXL_JOB_CRON_OPERATION_CLEANUP_OPERATION_HANDLER = "0 0 0/1 * * ?";

    @Override
    public void createOperation(
            String requestId, String requestUri, String parameters) throws ServiceException {
        OperationDo operationDo = new OperationDo();
        operationDo.setRequestId(requestId);
        operationDo.setRequestUri(requestUri);
        operationDo.setParameters(parameters);
        BaseDo.create(operationDo, InfrastructureConstants.ROOT_USERNAME, new java.util.Date());

        boolean ret = this.operationBlockingQueue.offer(operationDo);
        if (!ret) {
            LOGGER.warn("[LOG700] failed to save operationDo: {}", operationDo);
        }
    }

    @Override
    public void createOperationTask(
            String requestId, String task, String input, String output) throws ServiceException {
        OperationTaskDo operationTaskDo = new OperationTaskDo();
        operationTaskDo.setRequestId(requestId);
        operationTaskDo.setTask(task);
        operationTaskDo.setInput(input);
        operationTaskDo.setOutput(output);
        BaseDo.create(operationTaskDo, InfrastructureConstants.ROOT_USERNAME, new java.util.Date());

        boolean ret = this.operationTaskBlockingQueue.offer(operationTaskDo);
        if (!ret) {
            LOGGER.warn("[LOG700] failed to save operationTaskDo: {}", operationTaskDo);
        }
    }

    /**
     * 周期调度批量保存operation，每两次调度间隔2秒
     */
    @Scheduled(cron = XXL_JOB_CRON_OPERATION_SAVE_OPERATION_HANDLER)
    public void batchSaveOperation() {
        List<OperationDo> operationDoList = new ArrayList<>(400);
        int i = 0;
        while (i < 400) {
            OperationDo operationDo = this.operationBlockingQueue.poll();
            if (operationDo == null) {
                break;
            }

            operationDoList.add(operationDo);

            i++;
        }

        if (CollectionUtils.isNotEmpty(operationDoList)) {
            this.operationRepository.saveAll(operationDoList);
        }
    }

    /**
     * 周期调度批量保存operation task，每两次调度间隔2秒
     */
    @Scheduled(cron = XXL_JOB_CRON_OPERATION_SAVE_OPERATION_TASK_HANDLER)
    public void batchSaveOperationTask() {
        List<OperationTaskDo> operationTaskDoList = new ArrayList<>(400);
        int i = 0;
        while (i < 400) {
            OperationTaskDo operationTaskDo = this.operationTaskBlockingQueue.poll();
            if (operationTaskDo == null) {
                break;
            }
            operationTaskDoList.add(operationTaskDo);

            i++;
        }

        if (CollectionUtils.isNotEmpty(operationTaskDoList)) {
            this.operationTaskRepository.saveAll(operationTaskDoList);
        }
    }

    /**
     * 周期调度批量清理过期operation and operation task(s)，每两次调度间隔1小时
     */
    @Scheduled(cron = XXL_JOB_CRON_OPERATION_CLEANUP_OPERATION_HANDLER)
    public void batchCleanupExpiredOperation() {
        LOGGER.info("[AUDIT100]cleanup expired operation");

        DateTime nowDateTime = new DateTime(new java.util.Date());
        DateTime thresholdDateTime = nowDateTime.minusDays(this.auditDeleteOperationExistsDays);
        java.util.Date expiresAt = thresholdDateTime.toDate();

        // 获取分布式锁

        this.operationTaskRepository.deleteBeforeCreateTimestamp(expiresAt);

        this.operationRepository.deleteBeforeCreateTimestamp(expiresAt);
    }

    @Override
    public PageResult<OperationDto> pagingQueryOperation(
            String requestId,
            String requestUri,
            String parameters,
            Pageable pageable) throws ServiceException {
        Specification<OperationDo> specification = new Specification<OperationDo>() {
            @Override
            public Predicate toPredicate(Root<OperationDo> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (requestId != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("requestId"), requestId));
                }
                if (!Strings.isNullOrEmpty(requestUri)) {
                    predicateList.add(criteriaBuilder.like(root.get("requestUri"), "%" + requestUri + "%"));
                }
                if (!Strings.isNullOrEmpty(parameters)) {
                    predicateList.add(criteriaBuilder.like(root.get("parameters"), "%" + parameters + "%"));
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        };

        if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
            Sort sort = Sort.by(Sort.Order.desc("id"));
            PageRequest replacedPageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            pageable = replacedPageRequest;
        }

        Page<OperationDo> operationDoPage = this.operationRepository.findAll(specification, pageable);

        PageResult<OperationDto> operationDtoPageResult = new PageResult<>();
        operationDtoPageResult.setTotalPages(operationDoPage.getTotalPages());
        operationDtoPageResult.setPageSize(operationDoPage.getSize());
        operationDtoPageResult.setPageNumber(operationDoPage.getNumber());
        operationDtoPageResult.setTotalElements(operationDoPage.getTotalElements());
        operationDtoPageResult.setNumberOfElements(operationDoPage.getNumberOfElements());

        if (!operationDoPage.isEmpty()) {
            operationDtoPageResult.setContent(new ArrayList<>());

            operationDoPage.getContent().forEach(operationDo -> {
                OperationDto operationDto = new OperationDto();
                BeanUtils.copyProperties(operationDo, operationDto);
                operationDtoPageResult.getContent().add(operationDto);
            });
        }

        return operationDtoPageResult;
    }

    @Override
    public OperationDetailsDto getDetailsOfOperationByRequestId(
            String requestId) throws ServiceException {
        OperationDo operationDo =
                this.operationRepository.findByRequestId(requestId);
        if (operationDo == null) {
            throw new ResourceNotFoundException(OperationDo.RESOURCE_NAME + "::" + "request_id:" + requestId);
        }

        OperationDetailsDto operationDetailsDto = new OperationDetailsDto();
        operationDetailsDto.setRequestId(requestId);
        operationDetailsDto.setRequestUri(operationDo.getRequestUri());
        operationDetailsDto.setParameters(operationDo.getParameters());
        operationDetailsDto.setCreateTimestamp(operationDo.getCreateTimestamp());

        List<OperationTaskDo> operationTaskDoList =
                this.operationTaskRepository.findByRequestIdWithOrderByCreateTimestampAsc(requestId);
        if (CollectionUtils.isNotEmpty(operationTaskDoList)) {
            operationDetailsDto.setTasks(new ArrayList<>());

            operationTaskDoList.forEach(operationTaskDo -> {
                OperationTaskDto operationTaskDto = new OperationTaskDto();
                operationTaskDto.setTask(operationTaskDo.getTask());
                operationTaskDto.setInput(operationTaskDo.getInput());
                operationTaskDto.setOutput(operationTaskDo.getOutput());
                operationTaskDto.setCreateTimestamp(operationTaskDo.getCreateTimestamp());

                operationDetailsDto.getTasks().add(operationTaskDto);
            });
        }

        return operationDetailsDto;
    }
}
