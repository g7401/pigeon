package io.g740.pigeon.biz.service.interfaces.deployment;

import com.alibaba.fastjson.JSONObject;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.export.AsyncExportJobDto;
import io.g740.pigeon.biz.share.constants.AsyncExportFileTypeEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
public interface DfDeploymentService {

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
    PageResult<DfSimpleDto> queryDfForPageGeneration(
            List<String> tags, String name, Pageable pageable, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取为DF生成页面的配置
     *
     * @param dfKey
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DfConfForPageGenerationDto getDfConfForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取为DF生成页面的配置中的表格配置
     *
     * @param dfKey
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DfConfTableFieldDto> getDfConfTableFieldsForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取为DF生成页面的配置中的表格配置中的允许group by字段
     *
     * @param dfKey
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DfConfTableFieldDto> getDfConfTableFieldsEnabledGroupByForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取为DF生成页面的配置中的表格配置中的填充了resource category字段
     *
     * @param dfKey
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DfConfTableFieldDto> getDfConfTableFieldsWithResourceCategoryForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询DF的内容
     *
     * @param dfKey              Data Facet的key
     * @param parameterMap       基础查询参数
     * @param dfAdvancedQueryDto 高级查询参数
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    PageResult<JSONObject> queryDfContent(
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException;

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
    List<JSONObject> queryDfContentForGraph(
            String dfKey,
            List<String> dimensionFieldNames,
            String kpiFieldName,
            Map<String, String[]> parameterMap,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 异步导出DF的内容
     *
     * @param asyncExportContentType
     * @param dfKey
     * @param parameterMap           基础查询参数
     * @param dfAdvancedQueryDto     高级查询参数
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AsyncExportJobDto asyncExportDfContent(
            AsyncExportFileTypeEnum asyncExportContentType,
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取指定异步导出工作
     *
     * @param uid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    AsyncExportJobDto getAsyncExportJob(Long uid,
                                        UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询异步导出工作
     *
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    PageResult<AsyncExportJobDto> queryAsyncExportJob(
            Pageable pageable, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除指定异步导出工作
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 取消指定异步导出工作
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void cancelAsyncExportJob(Long uid, UserInfo operationUserInfo) throws ServiceException;
}
