package io.g740.pigeon.biz.service.handler.openapi;

import com.google.common.base.Strings;
import io.g740.commons.exception.impl.*;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.app.*;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.object.entity.app.AppDfDo;
import io.g740.pigeon.biz.object.entity.app.AppDo;
import io.g740.pigeon.biz.object.entity.app.AppUserDo;
import io.g740.pigeon.biz.persistence.jpa.app.AppDfRepository;
import io.g740.pigeon.biz.persistence.jpa.app.AppRepository;
import io.g740.pigeon.biz.persistence.jpa.app.AppUserRepository;
import io.g740.pigeon.biz.service.interfaces.admin.DfService;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author bbottong
 */
@Handler
public class AppHandler {
    @Autowired
    private IdHelper idHelper;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppDfRepository appDfRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private DfService dfService;

    public String generateAppKey(UserInfo operationUserInfo) throws ServiceException {
        String uuid = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAppSecret(UserInfo operationUserInfo) throws ServiceException {
        String uuid = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(rollbackFor = Exception.class)
    public AppDto createApp(CreateAppDto createAppDto, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源重复
        boolean existsDuplicate = this.appRepository.existsByAppName(createAppDto.getAppName());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(AppDo.RESOURCE_NAME + ":" + createAppDto.getAppName());
        }
        existsDuplicate = this.appRepository.existsByAppKey(createAppDto.getAppKey());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(AppDo.RESOURCE_NAME + ":" + createAppDto.getAppKey());
        }

        // Step 2, 创建资源
        AppDo appDo = new AppDo();
        appDo.setUid(this.idHelper.getNextId(AppDo.RESOURCE_NAME));
        appDo.setAppName(createAppDto.getAppName());
        appDo.setAppKey(createAppDto.getAppKey());
        appDo.setAppSecret(createAppDto.getAppSecret());
        appDo.setEnabled(createAppDto.getEnabled());
        BaseDo.create(appDo, operationUserInfo.getUsername(), new Date());
        this.appRepository.save(appDo);

        // Step 3, 构建返回对象
        AppDto appDto = new AppDto();
        BeanUtils.copyProperties(appDo, appDto);

        return appDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppDto updateApp(UpdateAppDto updateAppDto, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源存在
        AppDo appDo = this.appRepository.findByUid(updateAppDto.getUid());
        if (appDo == null) {
            throw new ResourceNotFoundException(AppDo.RESOURCE_NAME + ":" + updateAppDto.getUid());
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(appDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        // Step 2, 更新资源
        boolean requiredToUpdate = false;
        if (updateAppDto.getEnabled() != null) {
            if (!updateAppDto.getEnabled().equals(appDo.getEnabled())) {
                requiredToUpdate = true;
                appDo.setEnabled(updateAppDto.getEnabled());
            }
        }
        if (!Strings.isNullOrEmpty(updateAppDto.getAppSecret()) &&
                !updateAppDto.getAppSecret().equalsIgnoreCase(appDo.getAppSecret())) {
            requiredToUpdate = true;
            appDo.setAppSecret(updateAppDto.getAppSecret());
        }
        if (!Strings.isNullOrEmpty(updateAppDto.getAppName()) &&
                !updateAppDto.getAppName().equalsIgnoreCase(appDo.getAppName())) {
            boolean existsDuplicate = this.appRepository.existsByAppName(updateAppDto.getAppName());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(AppDo.RESOURCE_NAME + ":" + updateAppDto.getAppName());
            }

            requiredToUpdate = true;
            appDo.setAppName(updateAppDto.getAppName());
        }

        if (requiredToUpdate) {
            BaseDo.update(appDo, operationUserInfo.getUsername(), new Date());
            this.appRepository.save(appDo);
        }

        // Step 3, 构建返回对象
        AppDto appDto = new AppDto();
        BeanUtils.copyProperties(appDo, appDto);

        return appDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteApp(Long uid, UserInfo operationUserInfo) throws ServiceException {
        // 确认app存在
        AppDo appDo = this.appRepository.findByUid(uid);
        if (appDo == null) {
            throw new ResourceNotFoundException(AppDo.RESOURCE_NAME + ":" + uid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(appDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        // 删除app df
        boolean existsInUse = this.appDfRepository.existsByAppUid(uid);
        if (existsInUse) {
            this.appDfRepository.deleteByAppUid(uid);
        }

        // 删除app user
        existsInUse = this.appUserRepository.existsByAppUid(uid);
        if (existsInUse) {
            this.appUserRepository.deleteByAppUid(uid);
        }

        // 删除app
        this.appRepository.deleteByUid(uid);
    }

    public AppDto getApp(Long uid, UserInfo operationUserInfo) throws ServiceException {
        AppDo appDo = this.appRepository.findByUid(uid);
        if (appDo == null) {
            throw new ResourceInUseException(AppDo.RESOURCE_NAME + ":" + uid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(appDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        AppDto appDto = new AppDto();
        appDto.setUid(appDo.getUid());
        appDto.setAppName(appDo.getAppName());
        appDto.setAppKey(appDo.getAppKey());
        appDto.setAppSecret(appDo.getAppSecret());
        appDto.setEnabled(appDo.getEnabled());

        return appDto;
    }

    public PageResult<AppDto> queryApp(
            String appName, Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        Page<AppDo> page;
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!Strings.isNullOrEmpty(appName)) {
                page = this.appRepository.findByAppName(appName, pageable);
            } else {
                page = this.appRepository.findAll(pageable);
            }
        } else {
            if (!Strings.isNullOrEmpty(appName)) {
                page = this.appRepository.findByAppNameAndCreateBy(appName, operationUserInfo.getUsername(), pageable);
            } else {
                page = this.appRepository.findByCreateBy(operationUserInfo.getUsername(), pageable);
            }
        }

        PageResult<AppDto> pageResult = new PageResult<>();
        pageResult.setTotalPages(page.getTotalPages());
        pageResult.setTotalElements(page.getTotalElements());
        pageResult.setPageSize(page.getSize());
        pageResult.setPageNumber(page.getNumber());
        pageResult.setNumberOfElements(page.getNumberOfElements());
        if (page.hasContent()) {
            pageResult.setContent(new ArrayList<>());
            page.getContent().forEach(appDo -> {
                AppDto appDto = new AppDto();
                BeanUtils.copyProperties(appDo, appDto);
                pageResult.getContent().add(appDto);
            });
        }
        return pageResult;
    }

    public List<AppDfDto> getAppAccessToDf(Long appUid, UserInfo operationUserInfo) throws ServiceException {
        AppDo appDo = this.appRepository.findByUid(appUid);
        if (appDo == null) {
            throw new ResourceInUseException(AppDo.RESOURCE_NAME + ":" + appUid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(appDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        //
        // Step 1, 列出existing
        //
        List<Long> existingListOfDf = this.appDfRepository.findDfUidByAppUid(appUid);
        if (existingListOfDf == null) {
            existingListOfDf = new ArrayList<>();
        }

        //
        // Step 2, 列出available
        //
        // 该用户所创建的Df
        List<DfSimpleDto> availableListOfDf;
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            availableListOfDf = this.dfService.listAllDf(operationUserInfo);
        } else {
            availableListOfDf = this.dfService.listDfByCreatedBy(operationUserInfo.getUsername(), operationUserInfo);
        }
        if (CollectionUtils.isNotEmpty(availableListOfDf)) {
            List<AppDfDto> listOfAppDfDto = new ArrayList<>();

            for (DfSimpleDto dfSimpleDto : availableListOfDf) {
                AppDfDto appDfDto = new AppDfDto();
                BeanUtils.copyProperties(dfSimpleDto, appDfDto);

                appDfDto.setSelected(existingListOfDf.contains(dfSimpleDto.getDfUid()));

                listOfAppDfDto.add(appDfDto);
            }

            return listOfAppDfDto;
        }

        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceAppAccessToDf(ReplaceAppAccessToDfDto replaceAppAccessToDfDto,
                                     UserInfo operationUserInfo) throws ServiceException {
        Long appUid = replaceAppAccessToDfDto.getAppUid();

        AppDo appDo = this.appRepository.findByUid(appUid);
        if (appDo == null) {
            throw new ResourceInUseException(AppDo.RESOURCE_NAME + ":" + appUid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(appDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        boolean exists = this.appDfRepository.existsByAppUid(appUid);
        if (exists) {
            this.appDfRepository.deleteByAppUid(appUid);
        }

        Date now = new Date();
        List<AppDfDo> listOfAppDfDo = new ArrayList<>();
        for (Long dfUid : replaceAppAccessToDfDto.getDfUids()) {
            AppDfDo appDfDo = new AppDfDo();
            appDfDo.setAppUid(appUid);
            appDfDo.setDfUid(dfUid);
            BaseDo.create(appDfDo, operationUserInfo.getUsername(), now);
            listOfAppDfDo.add(appDfDo);
        }
        this.appDfRepository.saveAll(listOfAppDfDo);
    }

    public List<String> getAppAccessByUser(Long appUid, UserInfo operationUserInfo) throws ServiceException {
        AppDo appDo = this.appRepository.findByUid(appUid);
        if (appDo == null) {
            throw new ResourceInUseException(AppDo.RESOURCE_NAME + ":" + appUid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(appDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        return this.appUserRepository.findByAppUid(appUid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceAppAccessByUser(ReplaceAppAccessByUserDto replaceAppAccessByUserDto,
                                       UserInfo operationUserInfo) throws ServiceException {
        Long appUid = replaceAppAccessByUserDto.getAppUid();

        AppDo appDo = this.appRepository.findByUid(appUid);
        if (appDo == null) {
            throw new ResourceInUseException(AppDo.RESOURCE_NAME + ":" + appUid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(appDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        boolean exists = this.appUserRepository.existsByAppUid(appUid);
        if (exists) {
            this.appUserRepository.deleteByAppUid(appUid);
        }

        Date now = new Date();
        List<AppUserDo> listOfAppUserDo = new ArrayList<>();
        for (String username : replaceAppAccessByUserDto.getUsernames()) {
            AppUserDo appUserDo = new AppUserDo();
            appUserDo.setAppUid(appUid);
            appUserDo.setUsername(username);
            BaseDo.create(appUserDo, operationUserInfo.getUsername(), now);
            listOfAppUserDo.add(appUserDo);
        }
        this.appUserRepository.saveAll(listOfAppUserDo);
    }

    public List<Long> getAuthorizedDfOfUser(UserInfo operationUserInfo) throws ServiceException {
        List<Long> listOfAppUid = this.appUserRepository.findByUsername(operationUserInfo.getUsername());
        if (CollectionUtils.isNotEmpty(listOfAppUid)) {
            List<Long> listOfDfUid = this.appDfRepository.findDfUidByAppUidIn(listOfAppUid);
            if (CollectionUtils.isNotEmpty(listOfDfUid)) {
                // 由于不同的App可以包含相同的DF，因此需要对df去重
                List<Long> listOfAppUidWithoutDuplicates = new ArrayList<>(new HashSet<>(listOfDfUid));
                return listOfAppUidWithoutDuplicates;
            }
        }
        return null;
    }
}
