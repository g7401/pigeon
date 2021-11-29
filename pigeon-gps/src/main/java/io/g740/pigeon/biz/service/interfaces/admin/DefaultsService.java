package io.g740.pigeon.biz.service.interfaces.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.defaults.*;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 默认值服务
 *
 * @author bbottong
 */
public interface DefaultsService {
    /**
     * 创建默认值类目
     *
     * @param createDefaultsCategoryDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsCategoryDto createDefaultsCategory(
            CreateDefaultsCategoryDto createDefaultsCategoryDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新默认值类目
     *
     * @param updateDefaultsCategoryDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsCategoryDto updateDefaultsCategory(
            UpdateDefaultsCategoryDto updateDefaultsCategoryDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除默认值类目
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteDefaultsCategory(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询默认值类目
     *
     * @param names
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    PageResult<DefaultsCategoryDto> queryDefaultsCategory(
            List<String> names, Pageable pageable, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有默认值类目
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode listAllDefaultsCategory(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 创建默认值构建流程定义
     *
     * @param createDefaultsBuildProcessDefDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsBuildProcessDefDto createDefaultsBuildProcessDef(
            CreateDefaultsBuildProcessDefDto createDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询指定默认值类目的默认值构建流程定义
     *
     * @param defaultsCategoryUid 默认值类目的UID
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsBuildProcessDefDto queryDefaultsBuildProcessDef(
            Long defaultsCategoryUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取默认值构建流程定义
     *
     * @param processDefUid 默认值构建流程定义的UID
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsBuildProcessDefDto getDefaultsBuildProcessDef(
            Long processDefUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新默认值构建流程定义
     *
     * @param updateDefaultsBuildProcessDefDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsBuildProcessDefDto updateDefaultsBuildProcessDef(
            UpdateDefaultsBuildProcessDefDto updateDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 测试默认值构建流程定义
     *
     * @param testDefaultsBuildProcessDefDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode testDefaultsBuildProcessDef(
            TestDefaultsBuildProcessDefDto testDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询默认值构建流程实例
     *
     * @param processDefUid
     * @param parameterMap
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    PageResult<DefaultsBuildProcessInstDto> queryDefaultsBuildProcessInst(
            Long processDefUid,
            Map<String, String[]> parameterMap,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有默认值内容
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode listAllDefaultsContent(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出指定默认值类目的内容
     *
     * @param defaultsCategoryUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode getDefaultsContentByDefaultsCategory(
            Long defaultsCategoryUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 创建指定默认值类目的内容
     *
     * @param createDefaultsContentDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsContentDto createDefaultsContent(
            CreateDefaultsContentDto createDefaultsContentDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新指定默认值内容
     *
     * @param updateDefaultsContentDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DefaultsContentDto updateDefaultsContent(
            UpdateDefaultsContentDto updateDefaultsContentDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除指定默认值内容
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteDefaultsContent(Long uid, UserInfo operationUserInfo) throws ServiceException;
}
