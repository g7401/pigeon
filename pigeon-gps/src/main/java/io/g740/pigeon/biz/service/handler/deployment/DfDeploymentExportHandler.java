package io.g740.pigeon.biz.service.handler.deployment;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import io.g740.commons.api.RequestIdLogFilter;
import io.g740.commons.constants.AsyncJobStatusEnum;
import io.g740.commons.constants.CsvExportStrategyEnum;
import io.g740.commons.exception.impl.ResourceInUseException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceBusyException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.DateUtils;
import io.g740.commons.utils.ExcelUtils;
import io.g740.commons.utils.FileUtils;
import io.g740.components.audit.OperationService;
import io.g740.components.job.JobHandler;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.df.DfAdvancedQueryDto;
import io.g740.pigeon.biz.object.dto.df.DfQueryResultFieldDto;
import io.g740.pigeon.biz.object.dto.export.AsyncExportJobDto;
import io.g740.pigeon.biz.object.entity.df.DfConfAdvancedDo;
import io.g740.pigeon.biz.object.entity.df.DfConfDataFieldDo;
import io.g740.pigeon.biz.object.entity.df.DfDo;
import io.g740.pigeon.biz.object.entity.export.AsyncExportJobConfDo;
import io.g740.pigeon.biz.object.entity.export.AsyncExportJobDo;
import io.g740.pigeon.biz.persistence.jpa.df.DfConfAdvancedRepository;
import io.g740.pigeon.biz.persistence.jpa.df.DfConfDataFieldRepository;
import io.g740.pigeon.biz.persistence.jpa.df.DfRepository;
import io.g740.pigeon.biz.persistence.jpa.export.AsyncExportJobConfRepository;
import io.g740.pigeon.biz.persistence.jpa.export.AsyncExportJobRepository;
import io.g740.pigeon.biz.service.handler.ds.DsConnectionHandler;
import io.g740.pigeon.biz.service.handler.ds.SqlTranslationHandler;
import io.g740.pigeon.biz.service.handler.general.DfContentQueryHandler;
import io.g740.pigeon.biz.share.constants.AsyncExportFileTypeEnum;
import io.g740.pigeon.biz.share.constants.DataFieldRoleEnum;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author bbottong
 */
// removed by lzp@20210426 @Handler
public class DfDeploymentExportHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DfDeploymentExportHandler.class);

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private OperationService operationService;

    @Autowired
    private DfRepository dfRepository;

    @Autowired
    private DfConfDataFieldRepository dfConfDataFieldRepository;

    @Autowired
    private DfConfAdvancedRepository dfConfAdvancedRepository;

    @Autowired
    private DfContentQueryHandler dfContentQueryHandler;

    @Autowired
    private AsyncExportJobRepository asyncExportJobRepository;

    @Autowired
    private JobHandler jobHandler;

    @Autowired
    private DsConnectionHandler dsConnectionHandler;

    @Autowired
    private SqlTranslationHandler sqlTranslationHandler;

    @Autowired
    private AsyncExportJobConfRepository asyncExportJobConfRepository;


    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Value("${application.dir.general.project.export}")
    private String projectExportPath;

    @Value("${application.df.export.thread-pool-size}")
    private Integer exportThreadPoolSize;

    private final String PAGED_QUERY_PROGRESS_MESSAGE_TEMPLATE =
            "asyncExportJob: uid - %d, pageSize - %d, page - %d/%d";

    /**
     * 导出任务线程池
     */
    private ExecutorService executorService;

    private final Map<Long, Future> asyncExportJobFutureMap = new ConcurrentHashMap<>();

    public AsyncExportJobDto asyncExportDfContent(
            AsyncExportFileTypeEnum asyncExportContentType,
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException {
        if (this.executorService == null) {
            this.executorService = Executors.newFixedThreadPool(this.exportThreadPoolSize);
        }

        // Step 0, 检查繁忙程度，
        AsyncExportJobConfDo asyncExportJobConfDo = this.asyncExportJobConfRepository.findAll().iterator().next();
        //
        // 运行不能超过限制
        Integer countOfRunning = this.asyncExportJobRepository.countByStatus(AsyncJobStatusEnum.RUNNING);
        if (countOfRunning > asyncExportJobConfDo.getMaxRunningThreshold()) {
            throw new ServiceBusyException("exceeds the running capacity limit of the export task");
        }
        // 排队不能超过限制
        Integer countOfWaiting = this.asyncExportJobRepository.countByStatus(AsyncJobStatusEnum.WAITING);
        if (countOfWaiting > asyncExportJobConfDo.getMaxWaitingThreshold()) {
            throw new ServiceBusyException("exceeds the waiting capacity limit of the export task");
        }

        // Step 1, 检查DF存在
        // df定义
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }
        // df的table配置
        List<DfConfDataFieldDo> tableListOfDfConfDataFieldDo =
                this.dfConfDataFieldRepository.findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(dfDo.getUid());
        Map<String, DfConfDataFieldDo> mapOfTable = new HashMap<>();
        if (CollectionUtils.isNotEmpty(tableListOfDfConfDataFieldDo)) {
            tableListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                mapOfTable.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
            });
        } else {
            throw new ResourceNotFoundException(DfConfDataFieldDo.RESOURCE_NAME + ":" + DfDo.RESOURCE_NAME + ":" + dfKey);
        }
        // df的高级配置
        DfConfAdvancedDo dfConfAdvancedDo = this.dfConfAdvancedRepository.findByDfUid(dfDo.getUid());

        // Step 2, 构建返回字段顺序
        final List<String> requiredDataFieldNamesInOrder = new ArrayList<>();
        if (dfAdvancedQueryDto != null) {
            if (CollectionUtils.isNotEmpty(dfAdvancedQueryDto.getRequiredReturnFieldNames())) {
                dfAdvancedQueryDto.getRequiredReturnFieldNames().forEach(name -> {
                    requiredDataFieldNamesInOrder.add(name);
                });
            } else if (dfAdvancedQueryDto.getGroupBy() != null) {
                switch (dfAdvancedQueryDto.getGroupBy().getFieldProcessingType()) {
                    case RETURN_ONLY_SELECTED_FIELDS:
                        if (CollectionUtils.isNotEmpty(dfAdvancedQueryDto.getGroupBy().getSelectedGroupByFieldNames())) {
                            dfAdvancedQueryDto.getGroupBy().getSelectedGroupByFieldNames().forEach(name -> {
                                requiredDataFieldNamesInOrder.add(name);
                            });
                        }
                        tableListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                            if (DataFieldRoleEnum.KPI.equals(dfConfDataFieldDo.getRole())) {
                                requiredDataFieldNamesInOrder.add(dfConfDataFieldDo.getFieldName());
                            }
                        });

                        break;
                    case RETURN_ALL_FIELDS:
                        for (DfConfDataFieldDo dfConfDataFieldDo : tableListOfDfConfDataFieldDo) {
                            requiredDataFieldNamesInOrder.add(dfConfDataFieldDo.getFieldName());
                        }
                        break;
                    case ADVANCED:
                        if (CollectionUtils.isNotEmpty(dfAdvancedQueryDto.getGroupBy().getSelectedGroupByFieldNames())) {
                            dfAdvancedQueryDto.getGroupBy().getSelectedGroupByFieldNames().forEach(name -> {
                                requiredDataFieldNamesInOrder.add(name);
                            });
                        }
                        if (CollectionUtils.isNotEmpty(dfAdvancedQueryDto.getGroupBy().getResultFields())) {
                            for (DfQueryResultFieldDto resultField : dfAdvancedQueryDto.getGroupBy().getResultFields()) {
                                if (Strings.isNullOrEmpty(resultField.getNewFieldLabel())) {
                                    requiredDataFieldNamesInOrder.add(resultField.getFieldName());
                                } else {
                                    requiredDataFieldNamesInOrder.add(resultField.getNewFieldLabel());
                                }
                            }
                        }
                        break;
                }
            }
        }
        if (CollectionUtils.isEmpty(requiredDataFieldNamesInOrder)) {
            for (DfConfDataFieldDo dfConfDataFieldDo : tableListOfDfConfDataFieldDo) {
                requiredDataFieldNamesInOrder.add(dfConfDataFieldDo.getFieldName());
            }
        }

        // Step 3, 构建查询语句
        DfContentQueryHandler.QueryStatement queryStatement =
                this.dfContentQueryHandler.buildQueryStatementWithoutPagination(
                        dfKey, parameterMap, dfAdvancedQueryDto, sort, operationUserInfo);

        // Audit logging
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("dfKey", dfKey);
        inputMap.put("parameterMap", parameterMap);
        inputMap.put("dfAdvancedQueryDto", dfAdvancedQueryDto);
        inputMap.put("sort", sort);
        this.operationService.createOperationTask(
                MDC.get(RequestIdLogFilter.REQUEST_ID_KEY),
                "buildQueryStatementWithoutPagination",
                JSONObject.toJSONString(inputMap),
                queryStatement == null ? null : JSONObject.toJSONString(queryStatement));

        // Step 4, 创建异步导出工作
        AsyncExportJobDo asyncExportJobDo = new AsyncExportJobDo();
        asyncExportJobDo.setUid(this.idHelper.getNextId(AsyncExportJobDo.RESOURCE_NAME));
        asyncExportJobDo.setName(dfDo.getName());
        asyncExportJobDo.setContentType(asyncExportContentType);
        asyncExportJobDo.setStatus(AsyncJobStatusEnum.WAITING);
        asyncExportJobDo.setRequestType(AsyncExportJobDo.RequestTypeEnum.DF);
        BaseDo.create(asyncExportJobDo, operationUserInfo.getUsername(), new Date());
        this.asyncExportJobRepository.save(asyncExportJobDo);

        // Step 5, 调度执行
        try {
            Future future = this.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //
                    // Step 5.1, 准入，检查异步工作状态，更新异步工作状态
                    //
                    Long asyncExportJobUid = asyncExportJobDo.getUid();
                    AsyncExportJobDo existingAsyncExportJobDo =
                            DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
                    if (existingAsyncExportJobDo == null) {
                        LOGGER.warn("cannot find async export job : " + asyncExportJobUid);
                    }
                    if (!AsyncJobStatusEnum.WAITING.equals(existingAsyncExportJobDo.getStatus())) {
                        LOGGER.warn("existing async export job unexpected status: " +
                                existingAsyncExportJobDo.getStatus() + ", " + asyncExportJobUid);
                        return;
                    }

                    //
                    // Step 5.2, 更新状态：RUNNING
                    //
                    existingAsyncExportJobDo.setStatus(AsyncJobStatusEnum.RUNNING);
                    existingAsyncExportJobDo.setStartTimestamp(new Date());
                    BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);

                    //
                    // Step 5.3, 查询总行数
                    //
                    Long total = null;
                    try {
                        SimpleQueryResult countResult =
                                DfDeploymentExportHandler.this.dsConnectionHandler.executeQuery(
                                        queryStatement.getDsType(),
                                        queryStatement.getDsConnectionProfileProps(),
                                        queryStatement.getCountSqlStatement());

                        if (countResult != null &&
                                CollectionUtils.isNotEmpty(countResult.getColumnNames()) &&
                                CollectionUtils.isNotEmpty(countResult.getRows())) {
                            Map<String, Object> row0 = countResult.getRows().get(0);
                            String column0 = countResult.getColumnNames().get(0);
                            Object column0Value = row0.get(column0);
                            if (column0Value instanceof Short) {
                                total = ((Short) column0Value).longValue();
                            } else if (column0Value instanceof Integer) {
                                total = ((Integer) column0Value).longValue();
                            } else if (column0Value instanceof Long) {
                                total = (Long) column0Value;
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    } finally {
                        if (total == null || total == 0L) {
                            existingAsyncExportJobDo.setStatus(AsyncJobStatusEnum.FAILED);
                            existingAsyncExportJobDo.setFailedTimestamp(new Date());
                            BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                            DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);
                            // 记录异步工作失败原因
                            DfDeploymentExportHandler.this.jobHandler.saveJobFailedReason(
                                    AsyncExportJobDo.RESOURCE_NAME, asyncExportJobUid,
                                    "got total:0");
                            return;
                        }
                    }

                    //
                    // Step 5.4, 计算总页数
                    //
                    Integer exportPageSize = asyncExportJobConfDo.getPageSize();
                    Long mod = total % exportPageSize;
                    Long totalPages;
                    if (mod > 0) {
                        totalPages = total / exportPageSize + 1;
                    } else {
                        totalPages = total / exportPageSize;
                    }
                    Integer excelSegmentSize = asyncExportJobConfDo.getExcelSegmentSize();
                    Integer csvThreadCount = asyncExportJobConfDo.getCsvThreadCount();

                    LOGGER.info("asyncExportJob: " + "uid - " + asyncExportJobUid +
                            ", total elements - " + total +
                            ", page size - " + exportPageSize +
                            ", total pages - " + totalPages +
                            ", excel segment size - " + excelSegmentSize +
                            ", csv thread count - " + csvThreadCount);

                    // 更新总记录数和总页数
                    existingAsyncExportJobDo.setTotal(total.intValue());
                    existingAsyncExportJobDo.setTotalPages(totalPages.intValue());
                    existingAsyncExportJobDo.setPageSize(exportPageSize);
                    existingAsyncExportJobDo.setFieldsCount(requiredDataFieldNamesInOrder.size());
                    BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);

                    //
                    // Step 5.5, 执行分页查询导出
                    //
                    String filePathPrefix = DfDeploymentExportHandler.this.projectExportPath + File.separator + dfDo.getName() + "_" +
                            DateTime.now().getYear() + "_" + DateTime.now().toString(DfDeploymentExportHandler.this.dateFormatter) + "_" + DateTime.now().getMillisOfDay();
                    switch (asyncExportContentType) {
                        case EXCEL: {
                            // 导出文件名
                            String filePath = filePathPrefix + ".xlsx";
                            asyncExportToExcel(asyncExportJobUid,
                                    filePath,
                                    mapOfTable,
                                    requiredDataFieldNamesInOrder,
                                    queryStatement.getDsType(),
                                    queryStatement.getDsConnectionProfileProps(),
                                    queryStatement.getQuerySqlStatement(),
                                    total.intValue(),
                                    totalPages.intValue(),
                                    exportPageSize,
                                    excelSegmentSize,
                                    operationUserInfo);
                        }
                        break;
                        case CSV: {
                            // CSV导出高级配置
                            CsvExportStrategyEnum csvExportStrategy = CsvExportStrategyEnum.DEFAULT;
                            if (dfConfAdvancedDo != null) {
                                // CSV导出策略：针对特殊字符（半角逗号、双引号、换行符【CRLF】）的处理
                                // 默认不处理特殊字符
                                csvExportStrategy = dfConfAdvancedDo.getCsvExportStrategy();
                                csvExportStrategy = (csvExportStrategy == null ? CsvExportStrategyEnum.DEFAULT : csvExportStrategy);
                            }

                            // 导出文件名
                            String filePath = filePathPrefix + ".csv";
                            asyncExportToCsv(false,
                                    asyncExportJobUid,
                                    filePath,
                                    mapOfTable,
                                    requiredDataFieldNamesInOrder,
                                    queryStatement.getDsType(),
                                    queryStatement.getDsConnectionProfileProps(),
                                    queryStatement.getQuerySqlStatement(),
                                    total.intValue(),
                                    totalPages.intValue(),
                                    exportPageSize,
                                    csvThreadCount,
                                    csvExportStrategy,
                                    operationUserInfo);
                        }
                        break;
                        case CSV_AND_COMPRESS: {
                            // CSV导出高级配置
                            CsvExportStrategyEnum csvExportStrategy = CsvExportStrategyEnum.DEFAULT;
                            if (dfConfAdvancedDo != null) {
                                // CSV导出策略：针对特殊字符（半角逗号、双引号、换行符【CRLF】）的处理
                                // 默认不处理特殊字符
                                csvExportStrategy = dfConfAdvancedDo.getCsvExportStrategy();
                                csvExportStrategy = (csvExportStrategy == null ? CsvExportStrategyEnum.DEFAULT : csvExportStrategy);
                            }

                            // 导出文件名
                            String filePath = filePathPrefix + ".csv";
                            asyncExportToCsv(true,
                                    asyncExportJobUid,
                                    filePath,
                                    mapOfTable,
                                    requiredDataFieldNamesInOrder,
                                    queryStatement.getDsType(),
                                    queryStatement.getDsConnectionProfileProps(),
                                    queryStatement.getQuerySqlStatement(),
                                    total.intValue(),
                                    totalPages.intValue(),
                                    exportPageSize,
                                    csvThreadCount,
                                    csvExportStrategy,
                                    operationUserInfo);
                        }
                        break;
                    }
                }
            });

            // 审计async export job
            auditAsyncExportJob(asyncExportJobDo.getUid(), future);
        } catch (RejectedExecutionException e) {
            String errorMessage = "failed to submit export task" + "." + e.getMessage();
            LOGGER.error(errorMessage, e);

            handleFailed(asyncExportJobDo.getUid(), errorMessage, operationUserInfo);

            throw new ServiceBusyException("rejected");
        }

        //
        // Step 6, 构造返回
        //
        AsyncExportJobDto asyncExportJobDto = new AsyncExportJobDto();
        asyncExportJobDto.setUid(asyncExportJobDo.getUid());
        asyncExportJobDto.setName(asyncExportJobDo.getName());
        asyncExportJobDto.setRequestType(asyncExportJobDo.getRequestType());
        asyncExportJobDto.setStatus(asyncExportJobDo.getStatus());
        asyncExportJobDto.setProgress(asyncExportJobDo.getProgress());
        asyncExportJobDto.setProgressPercentage(asyncExportJobDo.getProgressPercentage());
        asyncExportJobDto.setTotal(asyncExportJobDo.getTotal());
        asyncExportJobDto.setTotalPages(asyncExportJobDo.getTotalPages());
        asyncExportJobDto.setPageSize(asyncExportJobDo.getPageSize());
        asyncExportJobDto.setRows(asyncExportJobDo.getRows());
        asyncExportJobDto.setFileLength(asyncExportJobDto.getFileLength());
        asyncExportJobDto.setFormattedFileLength(asyncExportJobDo.getFormattedFileLength());
        asyncExportJobDto.setFileName(asyncExportJobDo.getFileName());
        asyncExportJobDto.setCreateTimestamp(asyncExportJobDo.getCreateTimestamp());
        asyncExportJobDto.setCreateBy(asyncExportJobDo.getCreateBy());
        asyncExportJobDto.setLastUpdateTimestamp(asyncExportJobDo.getLastUpdateTimestamp());
        asyncExportJobDto.setLastUpdateBy(asyncExportJobDo.getLastUpdateBy());
        asyncExportJobDto.setStartTimestamp(asyncExportJobDo.getStartTimestamp());
        asyncExportJobDto.setDoneTimestamp(asyncExportJobDo.getDoneTimestamp());
        asyncExportJobDto.setFailedTimestamp(asyncExportJobDo.getFailedTimestamp());
        asyncExportJobDto.setCanceledTimestamp(asyncExportJobDo.getCanceledTimestamp());
        asyncExportJobDto.setSuspendedTimestamp(asyncExportJobDo.getSuspendedTimestamp());
        asyncExportJobDto.setFileReadyTimestamp(asyncExportJobDo.getFileReadyTimestamp());

        return asyncExportJobDto;
    }

    /**
     * 异步导出Excel
     *
     * @param asyncExportJobUid
     * @param filePath
     * @param mapOfTable
     * @param requiredDataFieldsInOrder
     * @param dsType
     * @param dsConnectionProfileProps
     * @param queryStatement
     * @param total
     * @param totalPages
     * @param pageSize
     * @param segmentSize
     * @param operationUserInfo
     */
    private void asyncExportToExcel(Long asyncExportJobUid,
                                    String filePath,
                                    Map<String, DfConfDataFieldDo> mapOfTable,
                                    List<String> requiredDataFieldsInOrder,
                                    DsTypeEnum dsType,
                                    String dsConnectionProfileProps,
                                    String queryStatement,
                                    Integer total,
                                    Integer totalPages,
                                    Integer pageSize,
                                    Integer segmentSize,
                                    UserInfo operationUserInfo) {
        // 标题行（1行）
        boolean headAdded = false;

        // 已经取了多少行，用于控制不要超过Excel的单Sheet的行数限制
        int fetchedContentRows = 0;

        // 总查询时长（毫秒）
        int totalQueryDuration = 0;
        // 总写文件时长（毫秒）
        int totalWriteFileDuration = 0;

        // 页码
        int pageNumber = 0;

        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(filePath).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("sheet1").build();


            // 准入，更新异步工作状态
            AsyncExportJobDo existingAsyncExportJobDo =
                    DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
            if (existingAsyncExportJobDo == null) {
                LOGGER.warn("cannot find asyncExportJob:" + asyncExportJobUid);
                return;
            }
            if (!AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                LOGGER.warn("expect asyncExportJob:" + asyncExportJobUid + " 's status is running, but now get: " + existingAsyncExportJobDo.getStatus());
                return;
            }

            do {
                //
                // Step 1, 查询
                //
                Date beginQueryDate = new Date();
                String pagedQueryProgressMessage =
                        String.format(PAGED_QUERY_PROGRESS_MESSAGE_TEMPLATE, asyncExportJobUid, pageSize, pageNumber + 1, totalPages);
                LOGGER.info(pagedQueryProgressMessage + ", query begin");
                // 在query sql尾部添加pagination clause
                String pagedQueryStatement = DfDeploymentExportHandler.this.sqlTranslationHandler.addPagination(
                        dsType, queryStatement, pageNumber, pageSize, operationUserInfo);
                SimpleQueryResult queryResult = this.dsConnectionHandler.executeQuery(
                        dsType, dsConnectionProfileProps, pagedQueryStatement);
                List<Map<String, Object>> content = queryResult.getRows();
                LOGGER.info(pagedQueryProgressMessage + ", query end, rows - " + content.size());
                totalQueryDuration += (new Date().getTime() - beginQueryDate.getTime());

                //
                // Step 2, 校验查询结果，检测数据内容是否发生了变化
                //
                if (pageNumber < totalPages - 1) {
                    // 非最后一页的content size应该等于page size
                    if (content.size() != pageSize) {
                        String errorMessage = String.format("got actual %d rows, but expect %d rows for %s", content.size(), pageSize, pagedQueryProgressMessage);
                        LOGGER.error(errorMessage);
                        handleFailed(asyncExportJobUid, errorMessage, operationUserInfo);
                        return;
                    }
                } else {
                    // 最后一页的content size应该等于total - 前面所有页 * page size
                    if (content.size() != (total - pageNumber * pageSize)) {
                        String errorMessage = String.format("got actual %d rows, but expect %d rows for %s", content.size(), (total - pageNumber * pageSize), pagedQueryProgressMessage);
                        LOGGER.error(errorMessage);
                        handleFailed(asyncExportJobUid, errorMessage, operationUserInfo);
                        return;
                    }
                }

                //
                // Step 3, 更新job进展
                //
                existingAsyncExportJobDo =
                        DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
                if (existingAsyncExportJobDo != null
                        && AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                    existingAsyncExportJobDo.setProgress((pageNumber + 1) + "/" + totalPages);
                    Double progressPercentage = (pageNumber + 1) * 100.0 / totalPages;
                    existingAsyncExportJobDo.setProgressPercentage(String.format("%d%%", progressPercentage.intValue()));
                    BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);
                } else if (existingAsyncExportJobDo == null) {
                    LOGGER.warn("cannot find asyncExportJob:" + asyncExportJobUid);
                    return;
                } else {
                    LOGGER.warn("expect asyncExportJob:" + asyncExportJobUid + " 's status is running, but now get: " + existingAsyncExportJobDo.getStatus());
                    return;
                }

                //
                // Step 4, 添加表头行
                //
                if (!headAdded) {
                    Map<String, Object> headTuple = content.get(0);
                    List<String> columnsOfHeadRow = new ArrayList<>();
                    for (String fieldName : requiredDataFieldsInOrder) {
                        if (headTuple.containsKey(fieldName)) {
                            if (mapOfTable.containsKey(fieldName) &&
                                    !Strings.isNullOrEmpty(mapOfTable.get(fieldName).getFieldLabel())) {
                                columnsOfHeadRow.add(mapOfTable.get(fieldName).getFieldLabel());
                            } else {
                                columnsOfHeadRow.add(fieldName);
                            }
                        }
                    }
                    List<List<String>> head = new ArrayList<>();
                    head.add(columnsOfHeadRow);
                    excelWriter.write(head, writeSheet);

                    headAdded = true;
                }

                //
                // Step 5, 添加内容
                //
                Date beginWriteFileDate = new Date();
                List<List<Object>> contentRows = new ArrayList<>();
                for (int no = 0; no < content.size(); no++) {
                    Map<String, Object> contentTuple = content.get(no);
                    if (fetchedContentRows >= ExcelUtils.MAX_ROWS_PER_SHEET) {
                        // 超过Excel的单sheet最大行数限制，停止写入
                        break;
                    }
                    fetchedContentRows++;

                    List<Object> columnsOfContentRow = new ArrayList<>();
                    contentRows.add(columnsOfContentRow);

                    // 遵守字段顺序
                    for (String fieldName : requiredDataFieldsInOrder) {
                        if (contentTuple.containsKey(fieldName)) {
                            columnsOfContentRow.add(contentTuple.get(fieldName));
                        }
                    }

                    if (no > 0 && no % segmentSize == 0) {
                        excelWriter.write(contentRows, writeSheet);
                        contentRows.clear();
                    }
                }

                if (!contentRows.isEmpty()) {
                    excelWriter.write(contentRows, writeSheet);
                }

                totalWriteFileDuration += (new Date().getTime() - beginWriteFileDate.getTime());

                pageNumber++;

                if (fetchedContentRows >= ExcelUtils.MAX_ROWS_PER_SHEET) {
                    // 超过Excel的单sheet最大行数限制，停止写入
                    break;
                }
            } while (pageNumber < totalPages);

            excelWriter.finish();
            excelWriter = null;

            //
            // Step 6, 上传文件
            //
            // 准入，更新异步工作状态
            existingAsyncExportJobDo =
                    DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
            if (existingAsyncExportJobDo != null &&
                    AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                if (fetchedContentRows > 0) {
                    // 上传文件
                    File file = new File(filePath);

                    // 更新job信息
                    existingAsyncExportJobDo.setRows(fetchedContentRows);
                    existingAsyncExportJobDo.setFileLength(file.length());
                    existingAsyncExportJobDo.setFormattedFileLength(FileUtils.formatFileLength(file.length()));
                    existingAsyncExportJobDo.setFileName(file.getName());
                    existingAsyncExportJobDo.setFileReadyTimestamp(new Date());
                    existingAsyncExportJobDo.setTotalQueryDuration(totalQueryDuration / 1000);
                    existingAsyncExportJobDo.setTotalWriteFileDuration(totalWriteFileDuration / 1000);
                    BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);

                    //
                    // TODO 为了使得pigeon尽量减少依赖，导出的文件存在本地。但是，多实例情形下，下载请求可能去往非导出文件的实例，此时，如何下载？
                    //
                    String sharedFileId = null;

                    // 更新job信息
                    existingAsyncExportJobDo =
                            DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
                    if (existingAsyncExportJobDo != null &&
                            AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                        existingAsyncExportJobDo.setSharedFileId(sharedFileId);
                        existingAsyncExportJobDo.setStatus(AsyncJobStatusEnum.DONE);
                        existingAsyncExportJobDo.setDoneTimestamp(new Date());
                        long totalDuration = existingAsyncExportJobDo.getDoneTimestamp().getTime() - existingAsyncExportJobDo.getCreateTimestamp().getTime();
                        existingAsyncExportJobDo.setTotalDuration((int) totalDuration / 1000);
                        existingAsyncExportJobDo.setFormattedTotalDuration(DateUtils.format(totalDuration));
                        BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                        DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);
                    } else if (existingAsyncExportJobDo == null) {
                        LOGGER.warn("cannot find asyncExportJob:" + asyncExportJobUid);
                    } else {
                        LOGGER.warn("expect asyncExportJob:" + asyncExportJobUid + " 's status is running, but now get: " + existingAsyncExportJobDo.getStatus());
                    }
                } else {
                    // 更新job信息
                    existingAsyncExportJobDo.setRows(0);
                    existingAsyncExportJobDo.setStatus(AsyncJobStatusEnum.FAILED);
                    existingAsyncExportJobDo.setFailedTimestamp(new Date());
                    BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);

                    // 记录异步工作失败原因
                    String errorMessage = "failed to execute asyncExportJob:" + asyncExportJobUid + "." + " Got total:" + existingAsyncExportJobDo.getTotal() + ", but fetch 0";
                    LOGGER.warn(errorMessage);
                    DfDeploymentExportHandler.this.jobHandler.saveJobFailedReason(AsyncExportJobDo.RESOURCE_NAME,
                            asyncExportJobUid, errorMessage);
                }
            } else if (existingAsyncExportJobDo == null) {
                LOGGER.warn("cannot find asyncExportJob:" + asyncExportJobUid);
            } else {
                LOGGER.warn("expect asyncExportJob:" + asyncExportJobUid + " 's status is running, but now get: " + existingAsyncExportJobDo.getStatus());
            }
        } catch (Exception e) {
            String errorMessage = "failed to execute asyncExportJob:" + asyncExportJobUid + "." + e.getMessage();
            LOGGER.error(errorMessage, e);

            handleFailed(asyncExportJobUid, errorMessage, operationUserInfo);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 异步导出CSV
     *
     * @param requiredCompress
     * @param asyncExportJobUid
     * @param filePath
     * @param mapOfTable
     * @param requiredDataFieldsInOrder
     * @param dsType
     * @param dsConnectionProfileProps
     * @param queryStatement
     * @param totalPages
     * @param pageSize
     * @param threadCount
     * @param csvExportStrategy
     * @param operationUserInfo
     */
    private void asyncExportToCsv(boolean requiredCompress,
                                  Long asyncExportJobUid,
                                  String filePath,
                                  Map<String, DfConfDataFieldDo> mapOfTable,
                                  List<String> requiredDataFieldsInOrder,
                                  DsTypeEnum dsType,
                                  String dsConnectionProfileProps,
                                  String queryStatement,
                                  Integer total,
                                  Integer totalPages,
                                  Integer pageSize,
                                  Integer threadCount,
                                  CsvExportStrategyEnum csvExportStrategy,
                                  UserInfo operationUserInfo) {
        // 标题行，1行
        boolean headAdded = false;

        // 已经取了多少行
        int fetchedContentRows = 0;

        // 总查询时长（毫秒）
        int totalQueryDuration = 0;
        // 总写文件时长（毫秒）
        int totalWriteFileDuration = 0;

        // 查询和导出
        int pageNumber = 0;

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
            // UTF-8 with BOM (utf8bom)
            // 0xEF
            fileOutputStream.write(239);
            // 0xBB
            fileOutputStream.write(187);
            // 0xBF
            fileOutputStream.write(191);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);

            // 准入，更新异步工作状态
            AsyncExportJobDo existingAsyncExportJobDo =
                    DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
            if (existingAsyncExportJobDo == null) {
                LOGGER.warn("cannot find asyncExportJob:" + asyncExportJobUid);
                return;
            }
            if (!AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                LOGGER.warn("expect asyncExportJob:" + asyncExportJobUid + " 's status is running, but now get: " + existingAsyncExportJobDo.getStatus());
                return;
            }

            do {
                //
                // Step 1, 查询
                //
                Date beginQueryDate = new Date();
                String pagedQueryProgressMessage =
                        String.format(PAGED_QUERY_PROGRESS_MESSAGE_TEMPLATE, asyncExportJobUid, pageSize, pageNumber + 1, totalPages);
                LOGGER.info(pagedQueryProgressMessage + ", query begin");

                // 在query sql尾部添加pagination clause
                String pagedQueryStatement = DfDeploymentExportHandler.this.sqlTranslationHandler.addPagination(dsType,
                        queryStatement, pageNumber, pageSize, operationUserInfo);
                SimpleQueryResult queryResult = this.dsConnectionHandler.executeQuery(dsType, dsConnectionProfileProps,
                        pagedQueryStatement);
                List<Map<String, Object>> content = queryResult.getRows();
                LOGGER.info(pagedQueryProgressMessage + ", query end, rows - " + content.size());
                totalQueryDuration += (new Date().getTime() - beginQueryDate.getTime());

                //
                // Step 2, 校验查询结果，检测数据内容是否发生了变化
                //
                if (pageNumber < totalPages - 1) {
                    // 非最后一页的content size应该等于page size
                    if (content.size() != pageSize) {
                        String errorMessage = String.format("got actual %d rows, but expect %d rows for %s", content.size(), pageSize, pagedQueryProgressMessage);
                        LOGGER.error(errorMessage);
                        handleFailed(asyncExportJobUid, errorMessage, operationUserInfo);
                        return;
                    }
                } else {
                    // 最后一页的content size应该等于total - 前面所有页 * page size
                    if (content.size() != (total - pageNumber * pageSize)) {
                        String errorMessage = String.format("got actual %d rows, but expect %d rows for %s", content.size(), (total - pageNumber * pageSize), pagedQueryProgressMessage);
                        LOGGER.error(errorMessage);
                        handleFailed(asyncExportJobUid, errorMessage, operationUserInfo);
                        return;
                    }
                }

                //
                // Step 3, 更新job进展
                //
                existingAsyncExportJobDo =
                        DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
                if (existingAsyncExportJobDo != null &&
                        AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                    existingAsyncExportJobDo.setProgress((pageNumber + 1) + "/" + totalPages);
                    Double progressPercentage = (pageNumber + 1) * 100.0 / totalPages;
                    existingAsyncExportJobDo.setProgressPercentage(String.format("%d%%", progressPercentage.intValue()));
                    BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);
                } else if (existingAsyncExportJobDo == null) {
                    LOGGER.warn("cannot find asyncExportJob:" + asyncExportJobUid);
                    return;
                } else {
                    LOGGER.warn("expect asyncExportJob:"
                            + asyncExportJobUid
                            + " 's status is running, but now get: " + existingAsyncExportJobDo.getStatus());
                    return;
                }

                fetchedContentRows += content.size();

                //
                // Step 4, 添加表头
                //
                if (!headAdded) {
                    Map<String, Object> headTuple = content.get(0);

                    StringBuilder headRow = new StringBuilder();
                    for (String fieldName : requiredDataFieldsInOrder) {
                        if (headTuple.containsKey(fieldName)) {
                            if (mapOfTable.containsKey(fieldName) &&
                                    !Strings.isNullOrEmpty(mapOfTable.get(fieldName).getFieldLabel())) {
                                headRow.append("\"").append(mapOfTable.get(fieldName).getFieldLabel()).append("\"").append(",");
                            } else {
                                headRow.append("\"").append(fieldName).append("\"").append(",");
                            }
                        }
                    }
                    headRow.deleteCharAt(headRow.length() - 1);
                    outputStreamWriter.append(headRow);

                    headAdded = true;
                }

                //
                // Step 5, 追加内容
                //
                Date beginWriteFileDate = new Date();
                if (content.size() > 1 / 3 * pageSize
                        && threadCount != null
                        && threadCount > 1
                        && content.size() > threadCount) {
                    int pieces = threadCount;
                    Integer sizeOfPiece = content.size() / pieces;
                    ExecutorService csvThreadPool = Executors.newFixedThreadPool(pieces);
                    List<SplitCsvRunnable> runnables = new ArrayList<>(pieces);
                    for (int pieceNo = 0; pieceNo < pieces - 1; pieceNo++) {
                        List<Map<String, Object>> contentOfPiece =
                                content.subList(pieceNo * sizeOfPiece, (pieceNo + 1) * sizeOfPiece);
                        SplitCsvRunnable runnableOfPiece =
                                new SplitCsvRunnable(contentOfPiece, requiredDataFieldsInOrder,
                                        csvExportStrategy, threadCount);
                        runnables.add(runnableOfPiece);
                        csvThreadPool.execute(runnableOfPiece);
                    }
                    List<Map<String, Object>> contentOfPiece =
                            content.subList((pieces - 1) * sizeOfPiece, content.size());
                    SplitCsvRunnable runnableOfPiece =
                            new SplitCsvRunnable(contentOfPiece, requiredDataFieldsInOrder,
                                    csvExportStrategy, threadCount);
                    runnables.add(runnableOfPiece);
                    csvThreadPool.execute(runnableOfPiece);
                    csvThreadPool.shutdown();
                    try {
                        csvThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

                        for (SplitCsvRunnable runnable : runnables) {
                            outputStreamWriter.append("\r\n").append(runnable.getLines());
                        }
                    } catch (InterruptedException e) {
                        String warnMessage = "failed to execute async export job for uid " + asyncExportJobUid + ". More" +
                                " info: " + e.getMessage() + ". Maybe the job is canceled by user.";
                        LOGGER.warn(warnMessage, e);

                        handleFailed(asyncExportJobUid, warnMessage, operationUserInfo);
                    }
                } else {
                    StringBuilder lines = new StringBuilder();
                    for (int no = 0; no < content.size(); no++) {
                        Map<String, Object> contentTuple = content.get(no);
                        // 遍历每一列
                        for (String fieldName : requiredDataFieldsInOrder) {
                            if (contentTuple.containsKey(fieldName)) {
                                Object fieldValue = contentTuple.get(fieldName);

                                if (fieldValue != null) {
                                    if (fieldValue instanceof String) {
                                        String fieldValueStr = (String) fieldValue;

                                        switch (csvExportStrategy) {
                                            case DEFAULT:
                                                lines.append(fieldValueStr).append(",");
                                                break;
                                            case REPLACE:
                                                fieldValueStr = fieldValueStr.replaceAll("[,]", " ");
                                                fieldValueStr = fieldValueStr.replaceAll("[\\r\\n]", " ");
                                                fieldValueStr = fieldValueStr.replaceAll("\"", " ");
                                                lines.append(fieldValueStr).append(",");
                                                break;
                                            case RFC:
                                                fieldValueStr = fieldValueStr.replaceAll("\"", "\"\"");
                                                lines.append("\"").append(fieldValueStr).append("\"").append(",");
                                                break;
                                        }
                                    } else {
                                        lines.append(fieldValue).append(",");
                                    }
                                } else {
                                    lines.append(",");
                                }
                            }
                        }
                        lines.deleteCharAt(lines.length() - 1);
                        lines.append("\r\n");
                    }
                    lines.deleteCharAt(lines.length() - 1);
                    lines.deleteCharAt(lines.length() - 1);
                    outputStreamWriter.append("\r\n").append(lines);
                }

                totalWriteFileDuration += (new Date().getTime() - beginWriteFileDate.getTime());

                pageNumber++;

            } while (pageNumber < totalPages);

            // 关闭文件
            outputStreamWriter.flush();
            outputStreamWriter.close();
            outputStreamWriter = null;

            //
            // Step 6, 文件上传，完成任务
            //
            // 准入，更新异步工作状态
            existingAsyncExportJobDo =
                    DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
            if (existingAsyncExportJobDo != null &&
                    AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                if (fetchedContentRows > 0) {
                    // 上传文件
                    File file = new File(filePath);
                    // 超出30MB自动压缩
                    if (requiredCompress || file.length() > 30 * 1000 * 1000) {
                        LOGGER.info("Begin to zip file for uid:" + existingAsyncExportJobDo.getUid() + " and file:" + filePath);
                        String zipFilePath = filePath.concat(".zip");
                        FileUtils.zipFile(filePath, zipFilePath);
                        file = new File(zipFilePath);
                        LOGGER.info("Done to zip file for uid:" + existingAsyncExportJobDo.getUid() + " and file:" + filePath + " and got zipped file:" + zipFilePath);
                    }

                    // 更新job信息
                    existingAsyncExportJobDo.setRows(fetchedContentRows);
                    existingAsyncExportJobDo.setFileLength(file.length());
                    existingAsyncExportJobDo.setFormattedFileLength(FileUtils.formatFileLength(file.length()));
                    existingAsyncExportJobDo.setFileName(file.getName());
                    existingAsyncExportJobDo.setFileReadyTimestamp(new Date());
                    existingAsyncExportJobDo.setTotalQueryDuration(totalQueryDuration / 1000);
                    existingAsyncExportJobDo.setTotalWriteFileDuration(totalWriteFileDuration / 1000);
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);

                    //
                    // TODO 为了使得pigeon尽量减少依赖，导出的文件存在本地。但是，多实例情形下，下载请求可能去往非导出文件的实例，此时，如何下载？
                    //
                    String sharedFileId = null;

                    // 更新job信息
                    existingAsyncExportJobDo =
                            DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
                    if (existingAsyncExportJobDo != null &&
                            AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
                        existingAsyncExportJobDo.setSharedFileId(sharedFileId);
                        existingAsyncExportJobDo.setStatus(AsyncJobStatusEnum.DONE);
                        existingAsyncExportJobDo.setDoneTimestamp(new Date());
                        long totalDuration = existingAsyncExportJobDo.getDoneTimestamp().getTime() - existingAsyncExportJobDo.getCreateTimestamp().getTime();
                        existingAsyncExportJobDo.setTotalDuration((int) totalDuration / 1000);
                        existingAsyncExportJobDo.setFormattedTotalDuration(DateUtils.format(totalDuration));
                        BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                        DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);
                    } else if (existingAsyncExportJobDo == null) {
                        LOGGER.warn("cannot find async export job : " + asyncExportJobUid);
                    } else {
                        LOGGER.warn("existing async export job unexpected status: " +
                                existingAsyncExportJobDo.getStatus() + ", " + asyncExportJobUid);
                    }
                } else {
                    // 更新job信息
                    existingAsyncExportJobDo.setRows(0);
                    existingAsyncExportJobDo.setStatus(AsyncJobStatusEnum.FAILED);
                    existingAsyncExportJobDo.setFailedTimestamp(new Date());
                    BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
                    DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);

                    // 记录异步工作失败原因
                    String errorMessage = "failed to execute asyncExportJob:" + asyncExportJobUid + "." + " Got total:" + existingAsyncExportJobDo.getTotal() + ", but fetch 0";
                    LOGGER.warn(errorMessage);
                    DfDeploymentExportHandler.this.jobHandler.saveJobFailedReason(AsyncExportJobDo.RESOURCE_NAME,
                            asyncExportJobUid, errorMessage);
                }
            } else if (existingAsyncExportJobDo == null) {
                LOGGER.warn("cannot find async export job : " + asyncExportJobUid);
            } else {
                LOGGER.warn("existing async export job unexpected status: " +
                        existingAsyncExportJobDo.getStatus() + ", " + asyncExportJobUid);
            }
        } catch (Exception e) {
            String errorMessage = "failed to execute async export job for uid " + asyncExportJobUid + ". More" +
                    " info: " + e.getMessage();
            LOGGER.error(errorMessage, e);

            handleFailed(asyncExportJobUid, errorMessage, operationUserInfo);
        } finally {
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    LOGGER.warn("failed to close " + filePath + ". " + e.getMessage(), e);
                }
            }
        }
    }

    private void handleFailed(Long asyncExportJobUid, String errorMessage, UserInfo operationUserInfo) {
        // 准入，更新异步工作状态
        AsyncExportJobDo existingAsyncExportJobDo =
                DfDeploymentExportHandler.this.asyncExportJobRepository.findByUid(asyncExportJobUid);
        if (existingAsyncExportJobDo != null &&
                AsyncJobStatusEnum.RUNNING.equals(existingAsyncExportJobDo.getStatus())) {
            existingAsyncExportJobDo.setStatus(AsyncJobStatusEnum.FAILED);
            existingAsyncExportJobDo.setFailedTimestamp(new Date());
            BaseDo.update(existingAsyncExportJobDo, operationUserInfo.getUsername(), new Date());
            DfDeploymentExportHandler.this.asyncExportJobRepository.save(existingAsyncExportJobDo);

            // 记录异步工作失败原因
            DfDeploymentExportHandler.this.jobHandler.saveJobFailedReason(AsyncExportJobDo.RESOURCE_NAME,
                    asyncExportJobUid,
                    errorMessage);
        } else if (existingAsyncExportJobDo == null) {
            LOGGER.warn("cannot find async export job : " + asyncExportJobUid);
        } else {
            LOGGER.warn("existing async export job unexpected status: " +
                    existingAsyncExportJobDo.getStatus() + ", " + asyncExportJobUid);
        }
    }

    public AsyncExportJobDto getAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException {
        AsyncExportJobDo asyncExportJobDo = this.asyncExportJobRepository.findByUid(uid);
        if (asyncExportJobDo == null) {
            throw new ResourceNotFoundException(AsyncExportJobDo.RESOURCE_NAME + ":" + uid);
        }

        AsyncExportJobDto asyncExportJobDto = new AsyncExportJobDto();
        BeanUtils.copyProperties(asyncExportJobDo, asyncExportJobDto);

        return asyncExportJobDto;
    }

    public PageResult<AsyncExportJobDto> queryAsyncExportJob(
            Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        PageRequest pageRequest;
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    pageable.getSort());
        } else {
            // 以下构建Sort的方式会导致找不到字段
            Sort sort = Sort.by(Sort.Order.desc("id"));
            pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        Page<AsyncExportJobDo> page = this.asyncExportJobRepository.findByCreateBy(operationUserInfo.getUsername(),
                pageRequest);

        PageResult<AsyncExportJobDto> pageResult = new PageResult<>();
        pageResult.setTotalPages(page.getTotalPages());
        pageResult.setTotalElements(page.getTotalElements());
        pageResult.setPageSize(page.getSize());
        pageResult.setPageNumber(page.getNumber());
        pageResult.setNumberOfElements(page.getNumberOfElements());
        if (CollectionUtils.isNotEmpty(page.getContent())) {
            List<AsyncExportJobDto> transformedContent = new ArrayList<>();
            page.getContent().forEach(asyncExportJobDo -> {
                AsyncExportJobDto asyncExportJobDto = new AsyncExportJobDto();
                BeanUtils.copyProperties(asyncExportJobDo, asyncExportJobDto);
                transformedContent.add(asyncExportJobDto);
            });
            pageResult.setContent(transformedContent);
        }

        return pageResult;
    }

    public void deleteAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException {
        AsyncExportJobDo asyncExportJobDo = this.asyncExportJobRepository.findByUidAndCreateBy(uid, operationUserInfo.getUsername());
        if (asyncExportJobDo == null) {
            throw new ResourceNotFoundException(AsyncExportJobDo.RESOURCE_NAME + "::" + "uid:" + uid + "," + "create_by:" + operationUserInfo.getUsername());
        }
        switch (asyncExportJobDo.getStatus()) {
            case CANCELED:
            case FAILED:
                asyncExportJobDo.setDeleted(true);
                BaseDo.update(asyncExportJobDo, operationUserInfo.getUsername(), new Date());
                this.asyncExportJobRepository.save(asyncExportJobDo);
                break;
            default:
                throw new ResourceInUseException(AsyncExportJobDo.RESOURCE_NAME + ":" + uid);
        }
    }

    public void cancelAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException {
        AsyncExportJobDo asyncExportJobDo = this.asyncExportJobRepository.findByUidAndCreateBy(uid, operationUserInfo.getUsername());
        if (asyncExportJobDo == null) {
            throw new ResourceNotFoundException(AsyncExportJobDo.RESOURCE_NAME + "::" + "uid:" + uid + "," + "create_by:" + operationUserInfo.getUsername());
        }
        switch (asyncExportJobDo.getStatus()) {
            case WAITING:
            case RUNNING:
                Future future = this.asyncExportJobFutureMap.get(uid);
                if (future != null) {
                    boolean ret = future.cancel(true);
                    if (ret) {
                        this.asyncExportJobFutureMap.remove(uid);

                        asyncExportJobDo.setStatus(AsyncJobStatusEnum.CANCELED);
                        asyncExportJobDo.setCanceledTimestamp(new Date());
                        BaseDo.update(asyncExportJobDo, operationUserInfo.getUsername(), new Date());
                        this.asyncExportJobRepository.save(asyncExportJobDo);
                    }
                }
                break;
            default:
                throw new ResourceInUseException(AsyncExportJobDo.RESOURCE_NAME + ":" + uid);
        }
    }

    private void auditAsyncExportJob(Long uid, Future future) {
        // Step 1, 已经完成的Future清理掉
        Iterator<Map.Entry<Long, Future>> entryIterator = this.asyncExportJobFutureMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Long, Future> entry = entryIterator.next();
            if (entry.getValue().isDone() || entry.getValue().isCancelled()) {
                entryIterator.remove();
            }
        }

        // Step 2, 加上新的Future
        this.asyncExportJobFutureMap.put(uid, future);
    }
}
