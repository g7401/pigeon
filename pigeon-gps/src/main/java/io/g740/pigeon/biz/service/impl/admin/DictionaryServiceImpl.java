package io.g740.pigeon.biz.service.impl.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.dictionary.*;
import io.g740.pigeon.biz.service.handler.admin.DictionaryBuildProcessHandler;
import io.g740.pigeon.biz.service.handler.admin.DictionaryHandler;
import io.g740.pigeon.biz.service.interfaces.admin.DictionaryService;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Service
public class DictionaryServiceImpl implements DictionaryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryServiceImpl.class);

    @Autowired
    private DictionaryHandler dictionaryHandler;

    @Autowired
    private DictionaryBuildProcessHandler dictionaryBuildProcessHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DictionaryCategoryDto createDictionaryCategory(
            CreateDictionaryCategoryDto createDictionaryCategoryDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.createDictionaryCategory(createDictionaryCategoryDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DictionaryCategoryDto updateDictionaryCategory(
            UpdateDictionaryCategoryDto updateDictionaryCategoryDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.updateDictionaryCategory(updateDictionaryCategoryDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteDictionaryCategory(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.dictionaryHandler.deleteDictionaryCategory(uid, operationUserInfo);
    }

    @Override
    public List<DictionaryCategoryDto> listAllDictionaryCategories(UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.listAllDictionaryCategories(operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DictionaryStructureDto createDictionaryStructure(
            CreateDictionaryStructureDto createDictionaryStructureDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.createDictionaryStructure(createDictionaryStructureDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DictionaryStructureDto updateDictionaryStructure(
            UpdateDictionaryStructureDto updateDictionaryStructureDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.updateDictionaryStructure(updateDictionaryStructureDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteDictionaryStructure(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.dictionaryHandler.deleteDictionaryStructure(uid, operationUserInfo);
    }

    @Override
    public SimpleTreeNode listAllDictionaryStructures(
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.listAllDictionaryStructures(operationUserInfo);
    }

    @Override
    public DictionaryBuildProcessDefDto createDictionaryBuildProcessDef(
            CreateDictionaryBuildProcessDefDto createDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.createDictionaryBuildProcessDef(createDictionaryBuildProcessDefDto, operationUserInfo);
    }

    @Override
    public DictionaryBuildProcessDefDto queryDictionaryBuildProcessDef(
            Long dictionaryCategoryUid,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.queryDictionaryBuildProcessDef(dictionaryCategoryUid, operationUserInfo);
    }

    @Override
    public DictionaryBuildProcessDefDto getDictionaryBuildProcessDef(
            Long processDefUid,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.getDictionaryBuildProcessDef(processDefUid, operationUserInfo);
    }

    @Override
    public DictionaryBuildProcessDefDto updateDictionaryBuildProcessDef(
            UpdateDictionaryBuildProcessDefDto updateDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.updateDictionaryBuildProcessDef(updateDictionaryBuildProcessDefDto,
                operationUserInfo);
    }

    @Override
    public SimpleTreeNode testDictionaryBuildProcessDef(
            TestDictionaryBuildProcessDefDto testDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryBuildProcessHandler.testDictionaryBuild(
                testDictionaryBuildProcessDefDto,
                operationUserInfo);
    }

    @Override
    public PageResult<DictionaryBuildProcessInstDto> queryDictionaryBuildProcessInst(
            Long processDefId,
            Map<String, String[]> parameterMap,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryBuildProcessHandler.queryDictionaryBuildProcessInst(
                processDefId, parameterMap, pageable, operationUserInfo);
    }

    @Override
    public SimpleTreeNode listAllDictionaryContent(UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.listAllDictionaryContent(operationUserInfo);
    }

    @Override
    public SimpleTreeNode getDictionaryContentByDictionaryCategory(
            Long dictionaryCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.getDictionaryContentByDictionaryCategory(dictionaryCategoryUid, operationUserInfo);
    }

    @Override
    public SimpleTreeNode listDictionaryContentByDictionaryCategoryWithSelectedDictionaryContent(
            Long dictionaryCategoryUid, List<Long> listOfSelectedDictionaryContentUid,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.listDictionaryContentByDictionaryCategoryWithSelectedDictionaryContent(
                dictionaryCategoryUid, listOfSelectedDictionaryContentUid, operationUserInfo
        );
    }

    @Override
    public SimpleTreeNode getNextLevelDictionaryContent(
            Long dictionaryCategoryUid, Long dictionaryContentUid,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.listNextLevelDictionaryContent(
                dictionaryCategoryUid, dictionaryContentUid, operationUserInfo);
    }

    @Override
    public SimpleTreeNode queryDictionaryContent(
            Long dictionaryCategoryUid, String label, UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.queryDictionaryContent(dictionaryCategoryUid, label, operationUserInfo);
    }

    @Override
    public DictionaryContentDto createDictionaryContent(
            CreateDictionaryContentDto createDictionaryContentDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.createDictionaryContent(createDictionaryContentDto, operationUserInfo);
    }

    @Override
    public DictionaryContentDto updateDictionaryContent(
            UpdateDictionaryContentDto updateDictionaryContentDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dictionaryHandler.updateDictionaryContent(updateDictionaryContentDto, operationUserInfo);
    }

    @Override
    public void deleteDictionaryContent(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.dictionaryHandler.deleteDictionaryContent(uid, operationUserInfo);
    }
}
