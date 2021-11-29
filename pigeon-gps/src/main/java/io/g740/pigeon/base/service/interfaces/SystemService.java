package io.g740.pigeon.base.service.interfaces;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.base.object.dto.SystemReleaseDto;
import io.g740.pigeon.base.object.dto.CreateSystemReleaseDto;

/**
 * @author bbottong
 */
public interface SystemService {
    /**
     * 创建系统发布
     *
     * @param createSystemReleaseDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SystemReleaseDto createSystemRelease(
            CreateSystemReleaseDto createSystemReleaseDto,
            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取系统最新的发布信息
     *
     * @return
     * @throws ServiceException
     */
    SystemReleaseDto getLatestSystemRelease() throws ServiceException;
}
