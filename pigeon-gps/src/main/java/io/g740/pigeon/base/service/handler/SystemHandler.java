package io.g740.pigeon.base.service.handler;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.base.object.dto.CreateSystemReleaseDto;
import io.g740.pigeon.base.object.dto.SystemReleaseDto;
import io.g740.pigeon.base.object.entity.SystemReleaseDo;
import io.g740.pigeon.base.persistence.jpa.SystemReleaseRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Handler
public class SystemHandler {
    @Autowired
    private IdHelper idHelper;

    @Autowired
    private SystemReleaseRepository systemReleaseRepository;

    @Transactional(rollbackFor = Exception.class)
    public SystemReleaseDto createSystemRelease(
            CreateSystemReleaseDto createSystemReleaseDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 将现有enabled的system release置为disabled
        List<SystemReleaseDo> listOfSystemReleaseDo = this.systemReleaseRepository.findEnabled();
        if (CollectionUtils.isNotEmpty(listOfSystemReleaseDo)) {
            listOfSystemReleaseDo.forEach(systemReleaseDo -> {
                systemReleaseDo.setEnabled(false);
                BaseDo.update(systemReleaseDo, operationUserInfo.getUsername(), new Date());
            });
            this.systemReleaseRepository.saveAll(listOfSystemReleaseDo);
        }

        // Step 2, 创建新的system release
        SystemReleaseDo systemReleaseDo = new SystemReleaseDo();
        systemReleaseDo.setEnabled(true);
        systemReleaseDo.setPrivacyPolicyUrl(createSystemReleaseDto.getPrivacyPolicyUrl());
        systemReleaseDo.setReleaseVersion(createSystemReleaseDto.getReleaseVersion());
        systemReleaseDo.setTermsOfServiceUrl(createSystemReleaseDto.getTermsOfServiceUrl());
        systemReleaseDo.setUid(this.idHelper.getNextId(SystemReleaseDo.RESOURCE_NAME));
        systemReleaseDo.setVendorName(createSystemReleaseDto.getVendorName());
        BaseDo.create(systemReleaseDo, operationUserInfo.getUsername(), new Date());
        this.systemReleaseRepository.save(systemReleaseDo);

        // Step 3, 构建返回对象
        SystemReleaseDto systemReleaseDto = new SystemReleaseDto();
        BeanUtils.copyProperties(systemReleaseDo, systemReleaseDto);

        return systemReleaseDto;
    }

    public SystemReleaseDto getLatestSystemRelease() throws ServiceException {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<SystemReleaseDo> page = this.systemReleaseRepository.findEnabledAndOrderByCreateTimestampDesc(pageRequest);
        if (page.hasContent()) {
            SystemReleaseDo systemReleaseDo = page.getContent().get(0);

            SystemReleaseDto systemReleaseDto = new SystemReleaseDto();
            BeanUtils.copyProperties(systemReleaseDo, systemReleaseDto);
            return systemReleaseDto;
        }

        return null;
    }
}
