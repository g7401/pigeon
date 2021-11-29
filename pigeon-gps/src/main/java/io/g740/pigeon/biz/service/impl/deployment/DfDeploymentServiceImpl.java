package io.g740.pigeon.biz.service.impl.deployment;

import com.alibaba.fastjson.JSONObject;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.export.AsyncExportJobDto;
import io.g740.pigeon.biz.service.handler.deployment.DfDeploymentQueryHandler;
import io.g740.pigeon.biz.service.handler.deployment.DfDeploymentStreamingExportHandler;
import io.g740.pigeon.biz.service.interfaces.deployment.DfDeploymentService;
import io.g740.pigeon.biz.share.constants.AsyncExportFileTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Service
public class DfDeploymentServiceImpl implements DfDeploymentService {
    @Autowired
    private DfDeploymentQueryHandler dfDeploymentQueryHandler;

    @Autowired
    private DfDeploymentStreamingExportHandler dfDeploymentStreamingExportHandler;

    /**
     * 查询DF
     *
     * @param tags
     * @param name
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public PageResult<DfSimpleDto> queryDfForPageGeneration(
            List<String> tags, String name, Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentQueryHandler.queryDfForPageGeneration(tags, name, pageable, operationUserInfo);
    }

    @Override
    public DfConfForPageGenerationDto getDfConfForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentQueryHandler.getDfConfForPageGeneration(dfKey, operationUserInfo);
    }

    @Override
    public List<DfConfTableFieldDto> getDfConfTableFieldsForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentQueryHandler.getDfConfTableFieldsForPageGeneration(dfKey, operationUserInfo);
    }

    @Override
    public List<DfConfTableFieldDto> getDfConfTableFieldsEnabledGroupByForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentQueryHandler.getDfConfTableFieldsEnabledGroupByForPageGeneration(dfKey, operationUserInfo);
    }

    @Override
    public PageResult<JSONObject> queryDfContent(
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentQueryHandler.queryDfContent(dfKey, parameterMap, dfAdvancedQueryDto, pageable, operationUserInfo);
    }

    /**
     * 查询DF的内容For Graph
     *
     * @param dfKey               Data Facet的key
     * @param dimensionFieldNames 维度字段名列表
     * @param kpiFieldName        KPI字段名
     * @param parameterMap        基础查询参数
     * @param sort                排序参数
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public List<JSONObject> queryDfContentForGraph(
            String dfKey,
            List<String> dimensionFieldNames,
            String kpiFieldName,
            Map<String, String[]> parameterMap,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentQueryHandler.queryDfContentForGraph(dfKey, dimensionFieldNames, kpiFieldName,
                parameterMap, sort, operationUserInfo);
    }

    @Override
    public AsyncExportJobDto asyncExportDfContent(
            AsyncExportFileTypeEnum asyncExportContentType,
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentStreamingExportHandler.asyncExportDfContent(asyncExportContentType, dfKey, parameterMap,
                dfAdvancedQueryDto, sort, operationUserInfo);
    }

    @Override
    public AsyncExportJobDto getAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentStreamingExportHandler.getAsyncExportJob(uid, operationUserInfo);
    }

    /**
     * 查询异步导出工作
     *
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public PageResult<AsyncExportJobDto> queryAsyncExportJob(
            Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDeploymentStreamingExportHandler.queryAsyncExportJob(pageable, operationUserInfo);
    }

    /**
     * 删除指定异步导出工作
     *
     * @param uid
     * @param operationUserInfo
     */
    @Override
    public void deleteAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.dfDeploymentStreamingExportHandler.deleteAsyncExportJob(uid, operationUserInfo);
    }

    /**
     * 取消指定异步导出工作
     *
     * @param uid
     * @param operationUserInfo
     */
    @Override
    public void cancelAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.dfDeploymentStreamingExportHandler.cancelAsyncExportJob(uid, operationUserInfo);
    }

    @Override
    public List<DfConfTableFieldDto> getDfConfTableFieldsWithResourceCategoryForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {

        return this.dfDeploymentQueryHandler.getDfConfTableFieldsWithResourceCategoryForPageGeneration(dfKey, operationUserInfo);
    }
}
