package io.g740.pigeon.biz.service.handler.admin;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.g740.commons.exception.impl.*;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.SingleInstanceUidGenerator;
import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.defaults.*;
import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.object.entity.defaults.DefaultsBuildProcessDefDo;
import io.g740.pigeon.biz.object.entity.defaults.DefaultsCategoryDo;
import io.g740.pigeon.biz.object.entity.defaults.DefaultsContentDo;
import io.g740.pigeon.biz.persistence.jpa.defaults.DefaultsBuildProcessDefRepository;
import io.g740.pigeon.biz.persistence.jpa.defaults.DefaultsCategoryRepository;
import io.g740.pigeon.biz.persistence.jpa.defaults.DefaultsContentRepository;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Handler
public class DefaultsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultsHandler.class);

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private SingleInstanceUidGenerator singleInstanceUidGenerator;

    @Autowired
    private DefaultsCategoryRepository defaultsCategoryRepository;

    @Autowired
    private DefaultsBuildProcessDefRepository defaultsBuildProcessDefRepository;

    @Autowired
    private DefaultsBuildProcessHandler defaultsBuildProcessHandler;

    @Autowired
    private DefaultsContentRepository defaultsContentRepository;

    public DefaultsCategoryDto getDefaultsCategory(Long defaultsCategoryUid) throws ServiceException {
        DefaultsCategoryDo defaultsCategoryDo = this.defaultsCategoryRepository.findByUid(defaultsCategoryUid);
        if (defaultsCategoryDo == null) {
            throw new ResourceNotFoundException(DefaultsCategoryDo.RESOURCE_NAME + ":" + defaultsCategoryUid);
        }

        DefaultsCategoryDto defaultsCategoryDto = new DefaultsCategoryDto();
        defaultsCategoryDto.setUidCode(defaultsCategoryDo.getUid());
        defaultsCategoryDto.setName(defaultsCategoryDo.getName());
        defaultsCategoryDto.setDescription(defaultsCategoryDo.getDescription());
        defaultsCategoryDto.setUidCode(defaultsCategoryDo.getUid());

        return defaultsCategoryDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DefaultsCategoryDto createDefaultsCategory(
            CreateDefaultsCategoryDto createDefaultsCategoryDto, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源重复
        // Step 1.1, 按规则：name不能重复
        boolean existsDuplicate = this.defaultsCategoryRepository.existsByName(createDefaultsCategoryDto.getName());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(DefaultsCategoryDo.RESOURCE_NAME + ":" +
                    "duplicate reason: " + createDefaultsCategoryDto.getName());
        }

        // Step 2, 创建资源
        DefaultsCategoryDo defaultsCategoryDo = new DefaultsCategoryDo();
        defaultsCategoryDo.setUid(this.idHelper.getNextId(DefaultsCategoryDo.RESOURCE_NAME));
        defaultsCategoryDo.setName(createDefaultsCategoryDto.getName());
        defaultsCategoryDo.setDescription(createDefaultsCategoryDto.getDescription());
        BaseDo.create(defaultsCategoryDo, operationUserInfo.getUsername(), new Date());
        this.defaultsCategoryRepository.save(defaultsCategoryDo);

        // Step 3, 构建返回对象
        DefaultsCategoryDto defaultsCategoryDto = new DefaultsCategoryDto();
        defaultsCategoryDto.setUidCode(defaultsCategoryDo.getUid());
        defaultsCategoryDto.setName(defaultsCategoryDo.getName());
        defaultsCategoryDto.setDescription(defaultsCategoryDo.getDescription());
        defaultsCategoryDto.setUidCode(defaultsCategoryDo.getUid());

        return defaultsCategoryDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DefaultsCategoryDto updateDefaultsCategory(
            UpdateDefaultsCategoryDto updateDefaultsCategoryDto, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 获取资源
        DefaultsCategoryDo defaultsCategoryDo =
                this.defaultsCategoryRepository.findByUid(updateDefaultsCategoryDto.getUid());
        if (defaultsCategoryDo == null) {
            throw new ResourceNotFoundException(DefaultsCategoryDo.RESOURCE_NAME + ":" +
                    updateDefaultsCategoryDto.getUid());
        }

        // Step 2, 检查资源重复
        // Step 2.1, 按规则：新的name不能跟现有资源的name重复
        if (!defaultsCategoryDo.getName().equalsIgnoreCase(updateDefaultsCategoryDto.getName())) {
            boolean existsDuplicate = this.defaultsCategoryRepository.existsByName(updateDefaultsCategoryDto.getName());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DefaultsCategoryDo.RESOURCE_NAME + ":" +
                        "duplicate reason: " + updateDefaultsCategoryDto.getName());
            }
        }

        // Step 3, 更新资源
        boolean requiredToUpdate = false;
        if (!Strings.isNullOrEmpty(updateDefaultsCategoryDto.getName()) &&
                !updateDefaultsCategoryDto.getName().equalsIgnoreCase(defaultsCategoryDo.getName())) {
            defaultsCategoryDo.setName(updateDefaultsCategoryDto.getName());
            requiredToUpdate = true;
        }
        if (!Strings.isNullOrEmpty(updateDefaultsCategoryDto.getDescription()) &&
                !updateDefaultsCategoryDto.getDescription().equalsIgnoreCase(defaultsCategoryDo.getDescription())) {
            defaultsCategoryDo.setDescription(updateDefaultsCategoryDto.getDescription());
            requiredToUpdate = true;
        }
        if (requiredToUpdate) {
            BaseDo.update(defaultsCategoryDo, operationUserInfo.getUsername(), new Date());
            this.defaultsCategoryRepository.save(defaultsCategoryDo);
        }

        // Step 4, 构建返回对象
        DefaultsCategoryDto defaultsCategoryDto = new DefaultsCategoryDto();
        defaultsCategoryDto.setUidCode(defaultsCategoryDo.getUid());
        defaultsCategoryDto.setName(defaultsCategoryDo.getName());
        defaultsCategoryDto.setDescription(defaultsCategoryDo.getDescription());
        defaultsCategoryDto.setUidCode(defaultsCategoryDo.getUid());

        return defaultsCategoryDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDefaultsCategory(
            Long uid, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源存在
        boolean exists = this.defaultsCategoryRepository.existsByUid(uid);
        if (!exists) {
            throw new ResourceNotFoundException(DefaultsCategoryDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 2, 检查资源是否正在使用

        // Step 3, 删除资源
        deleteDefaultsBuildProcessDef(uid, operationUserInfo);
        this.defaultsCategoryRepository.deleteByUid(uid);
    }

    public SimpleTreeNode listAllDefaultsCategory(UserInfo operationUserInfo) throws ServiceException {
        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();

        this.defaultsCategoryRepository.findAll().forEach(defaultsCategoryDo -> {
            SimpleTreeNode defaultsCategoryTreeNode = new SimpleTreeNode();
            defaultsCategoryTreeNode.setUidCode(defaultsCategoryDo.getUid());
            defaultsCategoryTreeNode.setName(defaultsCategoryDo.getName());
            defaultsCategoryTreeNode.setDescription(defaultsCategoryDo.getDescription());
            defaultsCategoryTreeNode.setType(DefaultsCategoryDo.TYPE);
            result.getChildren().add(defaultsCategoryTreeNode);
        });
        return result;
    }

    public PageResult<DefaultsCategoryDto> queryDefaultsCategory(List<String> names,
                                                                 Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        Page<DefaultsCategoryDo> page = this.defaultsCategoryRepository.findByNameIn(names, pageable);
        PageResult<DefaultsCategoryDto> result = new PageResult<>();
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setPageSize(page.getSize());
        result.setPageNumber(page.getNumber());

        if (page.hasContent()) {
            result.setContent(new ArrayList<>());

            for (DefaultsCategoryDo defaultsCategoryDo : page.getContent()) {
                DefaultsCategoryDto defaultsCategoryDto = new DefaultsCategoryDto();
                BeanUtils.copyProperties(defaultsCategoryDo, defaultsCategoryDto);
                result.getContent().add(defaultsCategoryDto);
            }
        }

        result.setNumberOfElements(page.getNumberOfElements());

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public DefaultsBuildProcessDefDto createDefaultsBuildProcessDef(
            CreateDefaultsBuildProcessDefDto createDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step1, 检查资源重复
        Long defaultsCategoryUid = createDefaultsBuildProcessDefDto.getDefaultsCategoryUid();
        DefaultsBuildProcessDefDo defaultsBuildProcessDefDo =
                this.defaultsBuildProcessDefRepository.findByDefaultsCategoryUid(defaultsCategoryUid);
        if (defaultsBuildProcessDefDo != null) {
            throw new ResourceDuplicateException(DefaultsBuildProcessDefDo.RESOURCE_NAME + "::"
                    + "defaults_category_uid:" + defaultsCategoryUid);
        }

        // Step 2, 创建资源
        defaultsBuildProcessDefDo = new DefaultsBuildProcessDefDo();
        defaultsBuildProcessDefDo.setUid(this.idHelper.getNextId(DefaultsBuildProcessDefDo.RESOURCE_NAME));
        defaultsBuildProcessDefDo.setEnabled(createDefaultsBuildProcessDefDto.getEnabled());
        defaultsBuildProcessDefDo.setScheduleType(createDefaultsBuildProcessDefDto.getScheduleType());
        defaultsBuildProcessDefDo.setScheduleTypeExtDetails(createDefaultsBuildProcessDefDto.getScheduleTypeExtDetails());
        defaultsBuildProcessDefDo.setBuildStrategyType(createDefaultsBuildProcessDefDto.getBuildStrategyType());

        String buildStrategyContent = null;
        switch (createDefaultsBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                buildStrategyContent = JSON.toJSONString(createDefaultsBuildProcessDefDto.getSqlBuildStrategyContent());
                break;
        }
        defaultsBuildProcessDefDo.setBuildStrategyContent(buildStrategyContent);

        defaultsBuildProcessDefDo.setDefaultsCategoryUid(createDefaultsBuildProcessDefDto.getDefaultsCategoryUid());
        BaseDo.create(defaultsBuildProcessDefDo, operationUserInfo.getUsername(), new Date());
        this.defaultsBuildProcessDefRepository.save(defaultsBuildProcessDefDo);

        // Step 3, 处理process调度
        if (createDefaultsBuildProcessDefDto.getEnabled() != null &&
                Boolean.TRUE.equals(createDefaultsBuildProcessDefDto.getEnabled())) {
            if (ScheduleTypeEnum.PERIODIC.equals(createDefaultsBuildProcessDefDto.getScheduleType())) {
                try {
                    this.defaultsBuildProcessHandler.initRegisterScheduling(
                            defaultsBuildProcessDefDo.getUid(),
                            createDefaultsBuildProcessDefDto.getScheduleTypeExtDetails());
                } catch (Exception e) {
                    String errorMessage = "failed to register scheduling for defaults build process def " +
                            defaultsBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                    throw new ServiceException(errorMessage, e);
                }
            }
        }

        // Step 4, 构建返回对象
        DefaultsBuildProcessDefDto defaultsBuildProcessDefDto = new DefaultsBuildProcessDefDto();
        defaultsBuildProcessDefDto.setUid(defaultsBuildProcessDefDo.getUid());
        defaultsBuildProcessDefDto.setEnabled(defaultsBuildProcessDefDo.getEnabled());
        defaultsBuildProcessDefDto.setScheduleType(defaultsBuildProcessDefDo.getScheduleType());
        defaultsBuildProcessDefDto.setScheduleTypeExtDetails(defaultsBuildProcessDefDo.getScheduleTypeExtDetails());
        defaultsBuildProcessDefDto.setBuildStrategyType(defaultsBuildProcessDefDo.getBuildStrategyType());
        defaultsBuildProcessDefDto.setSqlBuildStrategyContent(createDefaultsBuildProcessDefDto.getSqlBuildStrategyContent());
        defaultsBuildProcessDefDto.setDefaultsCategoryUid(defaultsBuildProcessDefDo.getDefaultsCategoryUid());

        return defaultsBuildProcessDefDto;
    }

    public DefaultsBuildProcessDefDto queryDefaultsBuildProcessDef(
            Long defaultsCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        DefaultsBuildProcessDefDo defaultsBuildProcessDefDo =
                this.defaultsBuildProcessDefRepository.findByDefaultsCategoryUid(defaultsCategoryUid);
        if (defaultsBuildProcessDefDo == null) {
            return null;
        }

        DefaultsBuildProcessDefDto defaultsBuildProcessDefDto = new DefaultsBuildProcessDefDto();
        BeanUtils.copyProperties(defaultsBuildProcessDefDo, defaultsBuildProcessDefDto);

        switch (defaultsBuildProcessDefDo.getBuildStrategyType()) {
            case SQL:
                SqlBuildStrategyContentDto sqlBuildStrategyContentDto =
                        JSON.parseObject(defaultsBuildProcessDefDo.getBuildStrategyContent(), SqlBuildStrategyContentDto.class);
                defaultsBuildProcessDefDto.setSqlBuildStrategyContent(sqlBuildStrategyContentDto);
                break;
        }

        return defaultsBuildProcessDefDto;
    }

    public DefaultsBuildProcessDefDto getDefaultsBuildProcessDef(
            Long processDefUid, UserInfo operationUserInfo) throws ServiceException {
        DefaultsBuildProcessDefDo defaultsBuildProcessDefDo = this.defaultsBuildProcessDefRepository.findByUid(processDefUid);
        if (defaultsBuildProcessDefDo == null) {
            throw new ResourceNotFoundException(DefaultsBuildProcessDefDo.RESOURCE_NAME + ":" + processDefUid);
        }

        DefaultsBuildProcessDefDto defaultsBuildProcessDefDto = new DefaultsBuildProcessDefDto();
        BeanUtils.copyProperties(defaultsBuildProcessDefDo, defaultsBuildProcessDefDto);

        switch (defaultsBuildProcessDefDo.getBuildStrategyType()) {
            case SQL:
                SqlBuildStrategyContentDto sqlBuildStrategyContentDto =
                        JSON.parseObject(defaultsBuildProcessDefDo.getBuildStrategyContent(), SqlBuildStrategyContentDto.class);
                defaultsBuildProcessDefDto.setSqlBuildStrategyContent(sqlBuildStrategyContentDto);
                break;
        }

        return defaultsBuildProcessDefDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DefaultsBuildProcessDefDto updateDefaultsBuildProcessDef(
            UpdateDefaultsBuildProcessDefDto updateDefaultsBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 获取资源
        DefaultsBuildProcessDefDo defaultsBuildProcessDefDo =
                this.defaultsBuildProcessDefRepository.findByUid(updateDefaultsBuildProcessDefDto.getUid());
        if (defaultsBuildProcessDefDo == null) {
            throw new ResourceNotFoundException(DefaultsBuildProcessDefDo.RESOURCE_NAME + ":" +
                    updateDefaultsBuildProcessDefDto.getUid());
        }

        // Step 2, 检查资源重复
        Long defaultsCategoryUid = updateDefaultsBuildProcessDefDto.getDefaultsCategoryUid();
        defaultsBuildProcessDefDo =
                this.defaultsBuildProcessDefRepository.findByDefaultsCategoryUid(defaultsCategoryUid);
        if (defaultsBuildProcessDefDo == null) {
            throw new ResourceNotFoundException(DefaultsBuildProcessDefDo.RESOURCE_NAME + "::"
                    + "defaults_category_uid:" + defaultsCategoryUid);
        } else if (!defaultsBuildProcessDefDo.getUid().equals(updateDefaultsBuildProcessDefDto.getUid())) {
            throw new ResourceDataIntegrityViolationException("found defaults_build_process_def:"
                    + defaultsBuildProcessDefDo.getUid() + " associated to"
                    + " defaults_category_uid:" + defaultsCategoryUid
                    + ", but incoming request trying to update defaults_build_process_def:"
                    + updateDefaultsBuildProcessDefDto.getUid() + " w/ defaults_category_uid:"
                    + updateDefaultsBuildProcessDefDto.getDefaultsCategoryUid());
        }

        // Step 3, 更新资源
        boolean requiredToUpdate = false;
        // 综合考虑enabled, schedule type, schedule ext details，三者的更新
        // 如果此前enabled = true, schedule type = periodic，则scheduling已启动
        boolean alreadyRegisteredScheduling = false;
        boolean scheduleTypeExtDetailsChanged = false;
        if (defaultsBuildProcessDefDo.getEnabled() != null &&
                Boolean.TRUE.equals(defaultsBuildProcessDefDo.getEnabled()) &&
                ScheduleTypeEnum.PERIODIC.equals(defaultsBuildProcessDefDo.getScheduleType())) {
            alreadyRegisteredScheduling = true;
        }
        if (updateDefaultsBuildProcessDefDto.getEnabled() != null
                && !updateDefaultsBuildProcessDefDto.getEnabled().equals(defaultsBuildProcessDefDo.getEnabled())) {
            defaultsBuildProcessDefDo.setEnabled(updateDefaultsBuildProcessDefDto.getEnabled());
            requiredToUpdate = true;
        }
        if (updateDefaultsBuildProcessDefDto.getScheduleType() != null
                && !updateDefaultsBuildProcessDefDto.getScheduleType().equals(defaultsBuildProcessDefDo.getScheduleType())) {
            defaultsBuildProcessDefDo.setScheduleType(updateDefaultsBuildProcessDefDto.getScheduleType());
            requiredToUpdate = true;
        }
        if (updateDefaultsBuildProcessDefDto.getScheduleTypeExtDetails() != null
                && !updateDefaultsBuildProcessDefDto.getScheduleTypeExtDetails().equals(defaultsBuildProcessDefDo.getScheduleTypeExtDetails())) {
            defaultsBuildProcessDefDo.setScheduleTypeExtDetails(updateDefaultsBuildProcessDefDto.getScheduleTypeExtDetails());
            requiredToUpdate = true;

            scheduleTypeExtDetailsChanged = true;
        }
        if (updateDefaultsBuildProcessDefDto.getBuildStrategyType() != null
                && !updateDefaultsBuildProcessDefDto.getBuildStrategyType().equals(defaultsBuildProcessDefDo.getBuildStrategyType())) {
            defaultsBuildProcessDefDo.setBuildStrategyType(updateDefaultsBuildProcessDefDto.getBuildStrategyType());
            requiredToUpdate = true;
        }
        if (updateDefaultsBuildProcessDefDto.getBuildStrategyType() != null) {
            String updateBuildStrategyContent = null;
            switch (updateDefaultsBuildProcessDefDto.getBuildStrategyType()) {
                case SQL:
                    updateBuildStrategyContent = JSON.toJSONString(updateDefaultsBuildProcessDefDto.getSqlBuildStrategyContent());

                    break;
            }
            if (updateBuildStrategyContent != null &&
                    !updateBuildStrategyContent.equalsIgnoreCase(defaultsBuildProcessDefDo.getBuildStrategyContent())) {
                defaultsBuildProcessDefDo.setBuildStrategyContent(updateBuildStrategyContent);
                requiredToUpdate = true;
            }
        } else {
            String updateBuildStrategyContent = null;
            switch (defaultsBuildProcessDefDo.getBuildStrategyType()) {
                case SQL:
                    updateBuildStrategyContent = JSON.toJSONString(updateDefaultsBuildProcessDefDto.getSqlBuildStrategyContent());

                    break;
            }
            if (updateBuildStrategyContent != null &&
                    !updateBuildStrategyContent.equalsIgnoreCase(defaultsBuildProcessDefDo.getBuildStrategyContent())) {
                defaultsBuildProcessDefDo.setBuildStrategyContent(updateBuildStrategyContent);
                requiredToUpdate = true;
            }
        }

        if (updateDefaultsBuildProcessDefDto.getDefaultsCategoryUid() != null
                && !updateDefaultsBuildProcessDefDto.getDefaultsCategoryUid().equals(defaultsBuildProcessDefDo.getDefaultsCategoryUid())) {
            defaultsBuildProcessDefDo.setDefaultsCategoryUid(updateDefaultsBuildProcessDefDto.getDefaultsCategoryUid());
            requiredToUpdate = true;
        }
        if (requiredToUpdate) {
            BaseDo.update(defaultsBuildProcessDefDo, operationUserInfo.getUsername(), new Date());
            this.defaultsBuildProcessDefRepository.save(defaultsBuildProcessDefDo);
        }

        // Step 4, 处理process调度
        // 前面已经识别了enabled, schedule type, schedule type ext details三者的更新情况，此时，可以处理是否需要
        // register/revoke/update scheduling
        // 按最新的信息，是否需要register scheduling
        if (defaultsBuildProcessDefDo.getEnabled() != null &&
                Boolean.TRUE.equals(defaultsBuildProcessDefDo.getEnabled()) &&
                ScheduleTypeEnum.PERIODIC.equals(defaultsBuildProcessDefDo.getScheduleType())) {
            // 按最新的信息，需要register scheduling
            if (alreadyRegisteredScheduling) {
                // 此前已register，现在还需要，则继续考虑schedule type ext details
                if (scheduleTypeExtDetailsChanged) {
                    // ext details有变化，需要先revoke，再register
                    this.defaultsBuildProcessHandler.revokeScheduling(defaultsBuildProcessDefDo.getUid());
                    try {
                        this.defaultsBuildProcessHandler.initRegisterScheduling(defaultsBuildProcessDefDo.getUid(),
                                defaultsBuildProcessDefDo.getScheduleTypeExtDetails());
                    } catch (Exception e) {
                        String errorMessage = "failed to register scheduling for defaults build process def " +
                                defaultsBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                        throw new ServiceException(errorMessage, e);
                    }
                } else {
                    // ext details没有变化，DO NOTHING
                }
            } else {
                // 此前没有register，需要register
                try {
                    this.defaultsBuildProcessHandler.initRegisterScheduling(defaultsBuildProcessDefDo.getUid(),
                            defaultsBuildProcessDefDo.getScheduleTypeExtDetails());
                } catch (Exception e) {
                    String errorMessage = "failed to register scheduling for defaults build process def " +
                            defaultsBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                    throw new ServiceException(errorMessage, e);
                }
            }
        } else {
            // 按最新的信息，不需要register scheduling
            if (alreadyRegisteredScheduling) {
                // 此前已register，现在不需要，则执行revoke
                this.defaultsBuildProcessHandler.revokeScheduling(defaultsBuildProcessDefDo.getUid());
            } else {
                // 此前没有register，DO NOTHING
            }
        }

        // Step 5, 构建返回对象
        DefaultsBuildProcessDefDto defaultsBuildProcessDefDto = new DefaultsBuildProcessDefDto();
        BeanUtils.copyProperties(defaultsBuildProcessDefDo, defaultsBuildProcessDefDto);
        switch (defaultsBuildProcessDefDo.getBuildStrategyType()) {
            case SQL:
                SqlBuildStrategyContentDto sqlBuildStrategyContentDto =
                        JSON.parseObject(defaultsBuildProcessDefDo.getBuildStrategyContent(), SqlBuildStrategyContentDto.class);
                defaultsBuildProcessDefDto.setSqlBuildStrategyContent(sqlBuildStrategyContentDto);
                break;
        }

        return defaultsBuildProcessDefDto;
    }

    private void deleteDefaultsBuildProcessDef(
            Long defaultsCategoryUid,
            UserInfo operationUserInfo) throws ServiceException {
        DefaultsBuildProcessDefDo defaultsBuildProcessDefDo =
                this.defaultsBuildProcessDefRepository.findByDefaultsCategoryUid(defaultsCategoryUid);
        if (defaultsBuildProcessDefDo != null) {
            if (Boolean.TRUE.equals(defaultsBuildProcessDefDo.getEnabled())) {
                if (ScheduleTypeEnum.PERIODIC.equals(defaultsBuildProcessDefDo.getScheduleType())) {
                    try {
                        this.defaultsBuildProcessHandler.revokeScheduling(defaultsBuildProcessDefDo.getUid());
                    } catch (Exception e) {
                        String errorMessage = "failed to revoke scheduling for defaults build process def " +
                                defaultsBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                        throw new ServiceException(errorMessage, e);
                    }
                }
            }

            defaultsBuildProcessDefDo.setDeleted(Boolean.TRUE);
            BaseDo.update(defaultsBuildProcessDefDo, operationUserInfo.getUsername(), new java.util.Date());
            this.defaultsBuildProcessDefRepository.save(defaultsBuildProcessDefDo);
        }

    }

    public List<DefaultsContentDto> getDefaultsContent(
            Long defaultsCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        List<DefaultsContentDo> listOfDefaultsContentDo =
                this.defaultsContentRepository.findByDefaultsCategoryUidOrderByUidAsc(defaultsCategoryUid);
        if (CollectionUtils.isNotEmpty(listOfDefaultsContentDo)) {
            List<DefaultsContentDto> listOfDefaultsContentDto = new ArrayList<>();
            listOfDefaultsContentDo.forEach(defaultsContentDo -> {
                DefaultsContentDto defaultsContentDto = new DefaultsContentDto();
                defaultsContentDto.setUidCode(defaultsContentDo.getUid());
                defaultsContentDto.setDefaultsCategoryUidCode(defaultsContentDo.getDefaultsCategoryUid());
                defaultsContentDto.setLabel(defaultsContentDo.getLabel());
                defaultsContentDto.setValue(defaultsContentDo.getValue());
                listOfDefaultsContentDto.add(defaultsContentDto);
            });
            return listOfDefaultsContentDto;
        }

        return null;
    }

    public SimpleTreeNode listAllDefaultsContent(UserInfo operationUserInfo) throws ServiceException {
        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();

        this.defaultsCategoryRepository.findAll().forEach(defaultsCategoryDo -> {
            SimpleTreeNode defaultsCategoryTreeNode = new SimpleTreeNode();
            defaultsCategoryTreeNode.setUidCode(defaultsCategoryDo.getUid());
            defaultsCategoryTreeNode.setName(defaultsCategoryDo.getName());
            defaultsCategoryTreeNode.setDescription(defaultsCategoryDo.getDescription());
            defaultsCategoryTreeNode.setType(DefaultsCategoryDo.TYPE);
            result.getChildren().add(defaultsCategoryTreeNode);

            List<DefaultsContentDo> listOfDefaultsContentDo =
                    this.defaultsContentRepository.findByDefaultsCategoryUidOrderByUidAsc(defaultsCategoryDo.getUid());
            if (CollectionUtils.isNotEmpty(listOfDefaultsContentDo)) {
                defaultsCategoryTreeNode.setChildren(new ArrayList<>());

                listOfDefaultsContentDo.forEach(defaultsContentDo -> {
                    SimpleTreeNode defaultsContentTreeNode = new SimpleTreeNode();
                    defaultsContentTreeNode.setUidCode(defaultsContentDo.getUid());
                    defaultsContentTreeNode.setName(defaultsContentDo.getValue());
                    defaultsContentTreeNode.setDescription(defaultsContentDo.getLabel());
                    defaultsContentTreeNode.setType(DefaultsContentDo.TYPE);

                    defaultsCategoryTreeNode.getChildren().add(defaultsContentTreeNode);
                });
            }
        });

        return result;
    }

    public SimpleTreeNode getDefaultsContentByDefaultsCategory(
            Long defaultsCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();

        DefaultsCategoryDo defaultsCategoryDo =
                this.defaultsCategoryRepository.findByUid(defaultsCategoryUid);
        if (defaultsCategoryDo == null) {
            throw new ResourceNotFoundException(DefaultsCategoryDo.RESOURCE_NAME + ":" + defaultsCategoryUid);
        }

        SimpleTreeNode defaultsCategoryTreeNode = new SimpleTreeNode();
        defaultsCategoryTreeNode.setUidCode(defaultsCategoryDo.getUid());
        defaultsCategoryTreeNode.setName(defaultsCategoryDo.getName());
        defaultsCategoryTreeNode.setDescription(defaultsCategoryDo.getDescription());
        defaultsCategoryTreeNode.setType("category");
        result.getChildren().add(defaultsCategoryTreeNode);

        List<DefaultsContentDo> listOfDefaultsContentDo =
                this.defaultsContentRepository.findByDefaultsCategoryUidOrderByUidAsc(defaultsCategoryDo.getUid());
        if (CollectionUtils.isNotEmpty(listOfDefaultsContentDo)) {
            defaultsCategoryTreeNode.setChildren(new ArrayList<>());

            listOfDefaultsContentDo.forEach(defaultsContentDo -> {
                SimpleTreeNode defaultsContentTreeNode = new SimpleTreeNode();
                defaultsContentTreeNode.setUidCode(defaultsContentDo.getUid());
                defaultsContentTreeNode.setName(defaultsContentDo.getValue());
                defaultsContentTreeNode.setDescription(defaultsContentDo.getLabel());

                defaultsCategoryTreeNode.getChildren().add(defaultsContentTreeNode);
            });
        }

        return result;
    }

    public DefaultsContentDto createDefaultsContent(
            CreateDefaultsContentDto createDefaultsContentDto, UserInfo operationUserInfo) throws ServiceException {
// 允许重复
//       // Step 1, 检查资源重复
//        boolean existsDuplicate = this.defaultsContentRepository.existsByDefaultsCategoryUidAndValue(
//                createDefaultsContentDto.getDefaultsCategoryUid(),
//                createDefaultsContentDto.getValue());
//        if (existsDuplicate) {
//            throw new ResourceDuplicateException(DefaultsContentDo.RESOURCE_NAME + ":" +
//                    createDefaultsContentDto.getDefaultsCategoryUid() + "," +
//                    createDefaultsContentDto.getValue());
//        }
//        if (!Strings.isNullOrEmpty(createDefaultsContentDto.getLabel())) {
//            existsDuplicate = this.defaultsContentRepository.existsByDefaultsCategoryUidAndLabel(
//                    createDefaultsContentDto.getDefaultsCategoryUid(),
//                    createDefaultsContentDto.getLabel());
//            if (existsDuplicate) {
//                throw new ResourceDuplicateException(DefaultsContentDo.RESOURCE_NAME + ":" +
//                        createDefaultsContentDto.getDefaultsCategoryUid() + "," +
//                        createDefaultsContentDto.getLabel());
//            }
//        }

        // Step 2, 创建资源
        DefaultsContentDo defaultsContentDo = new DefaultsContentDo();
        defaultsContentDo.setUid(this.singleInstanceUidGenerator.generateUid(
                DefaultsContentDo.RESOURCE_NAME));
        defaultsContentDo.setValue(createDefaultsContentDto.getValue());
        if (Strings.isNullOrEmpty(createDefaultsContentDto.getLabel())) {
            defaultsContentDo.setLabel(createDefaultsContentDto.getValue());
        } else {
            defaultsContentDo.setLabel(createDefaultsContentDto.getLabel());
        }
        defaultsContentDo.setDefaultsCategoryUid(createDefaultsContentDto.getDefaultsCategoryUid());
        BaseDo.create(defaultsContentDo, operationUserInfo.getUsername(), new Date());
        this.defaultsContentRepository.save(defaultsContentDo);

        // Step 3, 构建返回对象
        DefaultsContentDto defaultsContentDto = new DefaultsContentDto();
        defaultsContentDto.setUidCode(defaultsContentDo.getUid());
        defaultsContentDto.setValue(defaultsContentDo.getValue());
        defaultsContentDto.setLabel(defaultsContentDo.getLabel());
        defaultsContentDto.setDefaultsCategoryUidCode(defaultsContentDo.getDefaultsCategoryUid());

        return defaultsContentDto;
    }

    public DefaultsContentDto updateDefaultsContent(
            UpdateDefaultsContentDto updateDefaultsContentDto, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源存在
        DefaultsContentDo defaultsContentDo = this.defaultsContentRepository.findByUid(updateDefaultsContentDto.getUid());
        if (defaultsContentDo == null) {
            throw new ResourceNotFoundException(DefaultsContentDo.RESOURCE_NAME + ":" + updateDefaultsContentDto.getUid());
        }

        // 允许重复
//        // Step 2, 检查资源重复
//        if (!Strings.isNullOrEmpty(updateDefaultsContentDto.getValue()) &&
//                !updateDefaultsContentDto.getValue().equalsIgnoreCase(defaultsContentDo.getValue())) {
//            boolean existsDuplicate = this.defaultsContentRepository.existsByDefaultsCategoryUidAndValue(
//                    defaultsContentDo.getDefaultsCategoryUid(),
//                    updateDefaultsContentDto.getValue());
//            if (existsDuplicate) {
//                throw new ResourceDuplicateException(DefaultsContentDo.RESOURCE_NAME + ":" +
//                        defaultsContentDo.getDefaultsCategoryUid() + "," +
//                        updateDefaultsContentDto.getValue());
//            }
//        }
//        if (!Strings.isNullOrEmpty(updateDefaultsContentDto.getLabel()) &&
//                !updateDefaultsContentDto.getLabel().equalsIgnoreCase(defaultsContentDo.getLabel())) {
//            boolean existsDuplicate = this.defaultsContentRepository.existsByDefaultsCategoryUidAndLabel(
//                    defaultsContentDo.getDefaultsCategoryUid(),
//                    updateDefaultsContentDto.getLabel());
//            if (existsDuplicate) {
//                throw new ResourceDuplicateException(DefaultsContentDo.RESOURCE_NAME + ":" +
//                        defaultsContentDo.getDefaultsCategoryUid() + "," +
//                        updateDefaultsContentDto.getLabel());
//            }
//        }

        // Step 3, 更新资源
        boolean requiredToUpdate = false;
        if (!Strings.isNullOrEmpty(updateDefaultsContentDto.getValue())
                && !updateDefaultsContentDto.getValue().equalsIgnoreCase(defaultsContentDo.getValue())) {
            requiredToUpdate = true;
            defaultsContentDo.setValue(updateDefaultsContentDto.getValue());
        }
        if (!Strings.isNullOrEmpty(updateDefaultsContentDto.getLabel())
                && !updateDefaultsContentDto.getLabel().equalsIgnoreCase(defaultsContentDo.getLabel())) {
            requiredToUpdate = true;
            defaultsContentDo.setLabel(updateDefaultsContentDto.getLabel());
        }
        if (requiredToUpdate) {
            BaseDo.update(defaultsContentDo, operationUserInfo.getUsername(), new Date());
            this.defaultsContentRepository.save(defaultsContentDo);
        }

        // Step 4, 构建返回对象
        DefaultsContentDto defaultsContentDto = new DefaultsContentDto();
        defaultsContentDto.setUidCode(defaultsContentDo.getUid());
        defaultsContentDto.setValue(defaultsContentDo.getValue());
        defaultsContentDto.setLabel(defaultsContentDo.getLabel());
        defaultsContentDto.setDefaultsCategoryUidCode(defaultsContentDo.getDefaultsCategoryUid());

        return defaultsContentDto;
    }

    public void deleteDefaultsContent(Long uid, UserInfo operationUserInfo) throws ServiceException {
        boolean exists = this.defaultsContentRepository.existsByUid(uid);
        if (!exists) {
            // work around: 不要抛出异常，dictionary content 在build的时候会先删再存新的，存在一个时间间隔，用户页面上显示的dictionary content实际已被删除。临时处理。
            return;

            // throw new ResourceNotFoundException(DefaultsContentDo.RESOURCE_NAME + ":" + uid);
        }

        this.defaultsContentRepository.deleteByUid(uid);
    }
}
