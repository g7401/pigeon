package io.g740.pigeon.biz.service.interfaces.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;

/**
 * 资源
 *
 * @author bbottong
 */
public interface ResourceService {
    /**
     * 列出所有资源类目及每个资源类目的结构
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode listAllResourceStructures(UserInfo operationUserInfo) throws ServiceException;
}
