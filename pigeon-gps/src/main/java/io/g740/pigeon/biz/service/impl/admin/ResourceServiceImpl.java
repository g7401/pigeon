package io.g740.pigeon.biz.service.impl.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.service.handler.admin.ResourceHandler;
import io.g740.pigeon.biz.service.interfaces.admin.ResourceService;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 资源服务
 *
 * @author bbottong
 */
@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private ResourceHandler resourceHandler;

    /**
     * 列出所有资源类目及每个资源类目的结构
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public SimpleTreeNode listAllResourceStructures(UserInfo operationUserInfo) throws ServiceException {
        return this.resourceHandler.listAllResourceStructures(operationUserInfo);
    }
}
