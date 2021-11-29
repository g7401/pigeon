package io.g740.pigeon.base.service.impl;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.base.object.dto.CreateSystemReleaseDto;
import io.g740.pigeon.base.object.dto.SystemReleaseDto;
import io.g740.pigeon.base.service.handler.SystemHandler;
import io.g740.pigeon.base.service.interfaces.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bbottong
 */
@Service
public class SystemServiceImpl implements SystemService {

    @Autowired
    private SystemHandler systemHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SystemReleaseDto createSystemRelease(
            CreateSystemReleaseDto createSystemReleaseDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.systemHandler.createSystemRelease(createSystemReleaseDto, operationUserInfo);
    }

    /**
     *  获取系统最新的发布信息
     *
     * @return
     * @throws ServiceException
     */
    @Override
    public SystemReleaseDto getLatestSystemRelease() throws ServiceException {
        return this.systemHandler.getLatestSystemRelease();
    }
}
