package io.g740.pigeon.biz.service.impl.admin;

import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.ds.*;
import io.g740.pigeon.biz.object.entity.ds.DsDo;
import io.g740.pigeon.biz.service.handler.ds.DsHandler;
import io.g740.pigeon.biz.service.interfaces.admin.DsService;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bbottong
 */
@Service
public class DsServiceImpl implements DsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DsServiceImpl.class);

    @Autowired
    private DsHandler dsHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DsDto createDs(CreateDsDto createDsDto,
                          UserInfo operationUserInfo) throws ServiceException {
        return this.dsHandler.createDs(createDsDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DsDto updateDs(UpdateDsDto updateDsDto,
                          UserInfo operationUserInfo) throws ServiceException {
        return this.dsHandler.updateDs(updateDsDto, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteDs(Long uid,
                         UserInfo operationUserInfo) throws ServiceException {
        this.dsHandler.deleteDs(uid, operationUserInfo);
    }

    @Override
    public DsDto getDs(Long uid, UserInfo operationUserInfo) throws ServiceException {
        return this.dsHandler.getDs(uid, operationUserInfo);
    }

    @Override
    public List<DsDto> listAllDs(UserInfo operationUserInfo) throws ServiceException {
        return this.dsHandler.listAllDs(operationUserInfo);
    }

    @Override
    public Boolean testDsConnection(TestDsConnectionDto testDsConnectionDto,
                                    UserInfo operationUserInfo) throws ServiceException {
        return this.dsHandler.testDsConnection(testDsConnectionDto, operationUserInfo);
    }

    /**
     * 测试在指定数据源上执行查询语句
     *
     * @param testQueryStatementDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public SimpleQueryResult testQueryStatement(
            TestQueryStatementDto testQueryStatementDto, UserInfo operationUserInfo) throws ServiceException {
        return this.dsHandler.testQueryStatement(testQueryStatementDto, operationUserInfo);
    }

    /**
     * 刷新数据源
     *
     * @param dsUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public DsRefreshResultDto refreshDs(Long dsUid, UserInfo operationUserInfo) throws ServiceException {
        return this.dsHandler.refreshDs(dsUid, operationUserInfo);
    }

    /**
     * 刷新所有数据源
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public List<DsRefreshResultDto> refreshAllDs(UserInfo operationUserInfo) throws ServiceException {
        List<DsDto> dsDtoList = this.dsHandler.listAllDs(operationUserInfo);
        if (CollectionUtils.isEmpty(dsDtoList)) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME);
        }

        List<DsRefreshResultDto> dsRefreshResultDtoList = new ArrayList<>();
        for (DsDto dsDto : dsDtoList) {
            DsRefreshResultDto dsRefreshResultDto = refreshDs(dsDto.getUid(), operationUserInfo);
            dsRefreshResultDtoList.add(dsRefreshResultDto);
        }

        return dsRefreshResultDtoList;
    }
}
