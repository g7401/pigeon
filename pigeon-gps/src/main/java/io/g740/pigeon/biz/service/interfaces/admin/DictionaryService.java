package io.g740.pigeon.biz.service.interfaces.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.dictionary.*;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
public interface DictionaryService {
    /**
     * 创建字典类目
     *
     * @param createDictionaryCategoryDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryCategoryDto createDictionaryCategory(CreateDictionaryCategoryDto createDictionaryCategoryDto,
                                                   UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新字典类目
     *
     * @param updateDictionaryCategoryDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryCategoryDto updateDictionaryCategory(UpdateDictionaryCategoryDto updateDictionaryCategoryDto,
                                                   UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除字典类目
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteDictionaryCategory(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有字典类目
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DictionaryCategoryDto> listAllDictionaryCategories(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 为指定字典类目创建结构
     *
     * @param createDictionaryStructureDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryStructureDto createDictionaryStructure(CreateDictionaryStructureDto createDictionaryStructureDto,
                                                     UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新字典结构
     *
     * @param updateDictionaryStructureDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryStructureDto updateDictionaryStructure(UpdateDictionaryStructureDto updateDictionaryStructureDto,
                                                     UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除字典结构
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteDictionaryStructure(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有字典类目及每个字典类目的结构
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode listAllDictionaryStructures(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 为指定字典类目创建字典构建流程定义
     *
     * @param createDictionaryBuildProcessDefDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryBuildProcessDefDto createDictionaryBuildProcessDef(
            CreateDictionaryBuildProcessDefDto createDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询字典构建流程定义
     *
     * @param dictionaryCategoryUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryBuildProcessDefDto queryDictionaryBuildProcessDef(
            Long dictionaryCategoryUid,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取字典构建流程定义
     *
     * @param processDefUid     字典构建流程定义的UID
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryBuildProcessDefDto getDictionaryBuildProcessDef(
            Long processDefUid,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新字典构建流程定义
     *
     * @param updateDictionaryBuildProcessDefDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryBuildProcessDefDto updateDictionaryBuildProcessDef(
            UpdateDictionaryBuildProcessDefDto updateDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 测试字典构建流程定义
     *
     * @param testDictionaryBuildProcessDefDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode testDictionaryBuildProcessDef(
            TestDictionaryBuildProcessDefDto testDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 查询字典构建流程实例
     *
     * @param processDefUid     字典构建流程定义的UID
     * @param parameterMap      查询参数
     * @param pageable          分页参数
     * @param operationUserInfo 用户信息
     * @return
     * @throws ServiceException
     */
    PageResult<DictionaryBuildProcessInstDto> queryDictionaryBuildProcessInst(
            Long processDefUid,
            Map<String, String[]> parameterMap,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有字典内容
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode listAllDictionaryContent(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 按dictionary category uid获取dictionary content
     *
     * @param dictionaryCategoryUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode getDictionaryContentByDictionaryCategory(
            Long dictionaryCategoryUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出指定字典类目的下一级字典内容以及选中字典内容的完整层级
     *
     * @param dictionaryCategoryUid
     * @param listOfSelectedDictionaryContentUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode listDictionaryContentByDictionaryCategoryWithSelectedDictionaryContent(
            Long dictionaryCategoryUid, List<Long> listOfSelectedDictionaryContentUid,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取指定dictionary category uid 和/或 dictionary content uid 的下一级字典内容
     *
     * @param dictionaryCategoryUid 不允许为空，
     * @param dictionaryContentUid  可以为空
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode getNextLevelDictionaryContent(
            Long dictionaryCategoryUid, Long dictionaryContentUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 模糊搜索指定dictionary category uid 的字典内容
     *
     * @param dictionaryCategoryUid
     * @param label
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode queryDictionaryContent(
            Long dictionaryCategoryUid, String label, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 创建字典内容
     *
     * @param createDictionaryContentDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryContentDto createDictionaryContent(CreateDictionaryContentDto createDictionaryContentDto,
                                                 UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新字典内容
     *
     * @param updateDictionaryContentDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DictionaryContentDto updateDictionaryContent(UpdateDictionaryContentDto updateDictionaryContentDto,
                                                 UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除字典内容
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteDictionaryContent(Long uid, UserInfo operationUserInfo) throws ServiceException;

}
