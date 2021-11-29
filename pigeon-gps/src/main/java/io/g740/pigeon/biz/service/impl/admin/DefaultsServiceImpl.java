package io.g740.pigeon.biz.service.impl.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.defaults.*;
import io.g740.pigeon.biz.service.handler.admin.DefaultsBuildProcessHandler;
import io.g740.pigeon.biz.service.handler.admin.DefaultsHandler;
import io.g740.pigeon.biz.service.interfaces.admin.DefaultsService;
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
public class DefaultsServiceImpl implements DefaultsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultsServiceImpl.class);

    @Autowired
    private DefaultsHandler defaultsHandler;

    @Autowired
    private DefaultsBuildProcessHandler defaultsBuildProcessHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DefaultsCategoryDto createDefaultsCategory(
            CreateDefaultsCategoryDto createDefaultsCategoryDto, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.createDefaultsCategory(createDefaultsCategoryDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DefaultsCategoryDto updateDefaultsCategory(
            UpdateDefaultsCategoryDto updateDefaultsCategoryDto, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.updateDefaultsCategory(updateDefaultsCategoryDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteDefaultsCategory(
            Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.defaultsHandler.deleteDefaultsCategory(uid, operationUserInfo);
    }

    @Override
    public SimpleTreeNode listAllDefaultsCategory(UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.listAllDefaultsCategory(operationUserInfo);
    }

    @Override
    public PageResult<DefaultsCategoryDto> queryDefaultsCategory(
            List<String> names, Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.queryDefaultsCategory(names, pageable, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DefaultsBuildProcessDefDto createDefaultsBuildProcessDef(
            CreateDefaultsBuildProcessDefDto createDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.createDefaultsBuildProcessDef(createDefaultsBuildProcessDefDto, operationUserInfo);
    }

    @Override
    public DefaultsBuildProcessDefDto queryDefaultsBuildProcessDef(
            Long defaultsCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.queryDefaultsBuildProcessDef(defaultsCategoryUid, operationUserInfo);
    }

    @Override
    public DefaultsBuildProcessDefDto getDefaultsBuildProcessDef(
            Long processDefUid, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.getDefaultsBuildProcessDef(processDefUid, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DefaultsBuildProcessDefDto updateDefaultsBuildProcessDef(
            UpdateDefaultsBuildProcessDefDto updateDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.updateDefaultsBuildProcessDef(updateDefaultsBuildProcessDefDto,
                operationUserInfo);
    }

    @Override
    public SimpleTreeNode testDefaultsBuildProcessDef(
            TestDefaultsBuildProcessDefDto testDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsBuildProcessHandler.testDefaultsBuildProcess(
                testDefaultsBuildProcessDefDto,
                operationUserInfo);
    }

    @Override
    public PageResult<DefaultsBuildProcessInstDto> queryDefaultsBuildProcessInst(
            Long processDefUid, Map<String, String[]> parameterMap,
            Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsBuildProcessHandler.queryDefaultsBuildProcessInst(
                processDefUid, parameterMap, pageable, operationUserInfo);
    }

    @Override
    public SimpleTreeNode listAllDefaultsContent(UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.listAllDefaultsContent(operationUserInfo);
    }


    @Override
    public SimpleTreeNode getDefaultsContentByDefaultsCategory(
            Long defaultsCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.getDefaultsContentByDefaultsCategory(defaultsCategoryUid, operationUserInfo);
    }

    @Override
    public DefaultsContentDto createDefaultsContent(
            CreateDefaultsContentDto createDefaultsContentDto, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.createDefaultsContent(createDefaultsContentDto, operationUserInfo);
    }

    @Override
    public DefaultsContentDto updateDefaultsContent(
            UpdateDefaultsContentDto updateDefaultsContentDto, UserInfo operationUserInfo) throws ServiceException {
        return this.defaultsHandler.updateDefaultsContent(updateDefaultsContentDto, operationUserInfo);
    }

    @Override
    public void deleteDefaultsContent(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.defaultsHandler.deleteDefaultsContent(uid, operationUserInfo);
    }
}
