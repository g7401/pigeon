package io.g740.pigeon.biz.service.interfaces.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.ds.*;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;

import java.util.List;

/**
 * @author bbottong
 * <p>
 * 数据源相关服务
 */
public interface DsService {
    /**
     * 创建数据源
     *
     * @param createDsDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DsDto createDs(CreateDsDto createDsDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 更新数据源
     *
     * @param updateDsDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DsDto updateDs(UpdateDsDto updateDsDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除数据源
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteDs(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 测试数据源连接
     *
     * @param testDsConnectionDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    Boolean testDsConnection(TestDsConnectionDto testDsConnectionDto,
                             UserInfo operationUserInfo) throws ServiceException;

    /**
     * 测试在指定数据源上执行查询语句
     *
     * @param testQueryStatementDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleQueryResult testQueryStatement(
            TestQueryStatementDto testQueryStatementDto, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取数据源
     *
     * @param uid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DsDto getDs(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有数据源
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DsDto> listAllDs(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 刷新数据源
     *
     * @param dsUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DsRefreshResultDto refreshDs(Long dsUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 刷新所有数据源
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DsRefreshResultDto> refreshAllDs(UserInfo operationUserInfo) throws ServiceException;
}
