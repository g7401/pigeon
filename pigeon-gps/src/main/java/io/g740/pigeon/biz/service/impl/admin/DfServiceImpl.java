package io.g740.pigeon.biz.service.impl.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.ds.IndexDto;
import io.g740.pigeon.biz.service.handler.admin.DfConfHandler;
import io.g740.pigeon.biz.service.handler.admin.DfDefHandler;
import io.g740.pigeon.biz.service.interfaces.admin.DfService;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
@Service
public class DfServiceImpl implements DfService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DfServiceImpl.class);

    @Autowired
    private DfDefHandler dfDefHandler;

    @Autowired
    private DfConfHandler dfConfHandler;

    /**
     * 创建或替换DF
     *
     * @param createOrReplaceDfDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DfDto createOrReplaceDf(CreateOrReplaceDfDto createOrReplaceDfDto,
                                   UserInfo operationUserInfo) throws ServiceException {
        // 创建DF
        DfDto dfDto;
        if (createOrReplaceDfDto.getUid() == null) {
            // 创建
            dfDto = this.dfDefHandler.createDf(createOrReplaceDfDto, operationUserInfo);
        } else {
            // 替换
            dfDto = this.dfDefHandler.replaceDf(createOrReplaceDfDto, operationUserInfo);
        }

        // 初始化DF的available data fields(可用字段)
        DfRefreshResultDto dfRefreshResultDto =
                this.dfConfHandler.initDfAvailableDataFields(dfDto.getUidCode(), operationUserInfo);
        if (!Boolean.TRUE.equals(dfRefreshResultDto.getSuccessful())) {
            throw new ServiceException("failed to initial load df available data fields for df " + dfDto.getUidCode());
        }

        // 如果DF从未配置过，则初始化配置
        boolean existsDfConf = this.dfConfHandler.existsDfConf(dfDto.getUidCode(), operationUserInfo);
        if (!existsDfConf) {
            this.dfConfHandler.initDfConf(dfDto.getUidCode(), operationUserInfo);
        }

        return dfDto;
    }

    /**
     * 删除DF
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteDf(Long uid, UserInfo operationUserInfo) throws ServiceException {
        this.dfDefHandler.deleteDf(uid, operationUserInfo);
    }

    /**
     * 获取DF
     *
     * @param uid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public DfDto getDfByUid(Long uid, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDefHandler.getDfByUid(uid, operationUserInfo);
    }

    @Override
    public DfSimpleDto getDfByKey(String key, UserInfo operationUserInfo) throws ServiceException {
        return this.dfDefHandler.getDfByKey(key, operationUserInfo);
    }

    /**
     * 列出所有Data Objects
     *
     * @param showDfOnly        true - 只显示Data Facets
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public SimpleTreeNode listAllDataObjectsWithOrWithoutDf(
            Boolean showDfOnly,
            UserInfo operationUserInfo) throws ServiceException {
        if (showDfOnly) {
            return this.dfDefHandler.listAllDataObjectsWithDf(operationUserInfo);
        } else {
            return this.dfDefHandler.listAllDataObjectsWithOrWithoutDf(operationUserInfo);
        }
    }

    /**
     * 获取DF的配置，包括基本配置和高级配置
     *
     * @param dfUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public DfConfDto getDfConf(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        return this.dfConfHandler.getDfConf(dfUid, operationUserInfo);
    }

    /**
     * 获取DF的配置，只包括基本配置
     *
     * @param dfUid
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public PageResult<DfConfDataFieldDto> getDfConfBasic(Long dfUid, Pageable pageable,
                                                         UserInfo operationUserInfo) throws ServiceException {
        return this.dfConfHandler.getDfConfBasic(dfUid, pageable, operationUserInfo);
    }

    /**
     * 获取DF的配置，只包括高级配置
     *
     * @param dfUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public DfConfAdvancedDto getDfConfAdvanced(Long dfUid,
                                               UserInfo operationUserInfo) throws ServiceException {
        return dfConfHandler.getDfConfAdvanced(dfUid, operationUserInfo);
    }

    /**
     * 创建或者更新DF的配置，包括基本配置和高级配置
     *
     * @param createOrReplaceDfConfDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DfConfDto createOrReplaceDfConf(
            CreateOrReplaceDfConfDto createOrReplaceDfConfDto,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dfConfHandler.createOrReplaceDfConf(createOrReplaceDfConfDto, operationUserInfo);
    }

    /**
     * 刷新DF的可用字段，即从Data Facet所依存的表/视图重新加载字段
     *
     * @param dfUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DfRefreshResultDto refreshDfAvailableDataFields(
            Long dfUid,
            UserInfo operationUserInfo) throws ServiceException {
        DfRefreshResultDto dfRefreshResultDto = this.dfConfHandler.refreshDfAvailableDataFields(dfUid, operationUserInfo);

        // 如果DF从未配置过，则初始化配置
        // 如果DF已经配置过，即使refresh df available data fields导致了字段变更，变更也要让用户处理
        boolean existsDfConf = this.dfConfHandler.existsDfConf(dfUid, operationUserInfo);
        if (existsDfConf) {
            this.dfConfHandler.reinitDfConf(dfUid, operationUserInfo);
        } else {
            this.dfConfHandler.initDfConf(dfUid, operationUserInfo);
        }

        return dfRefreshResultDto;
    }

    /**
     * 将Source DF的CONF复制给Destination DF
     *
     * @param sourceDfUid
     * @param destinationDfUid
     * @param operationUserInfo
     * @throws ServiceException
     */
    @Override
    public void replicateDfConf(
            Long sourceDfUid, Long destinationDfUid,
            UserInfo operationUserInfo) throws ServiceException {
        this.dfConfHandler.replicateDfConf(sourceDfUid, destinationDfUid, operationUserInfo);
    }

    /**
     * 加载指定Data Facet所依存的表的索引
     *
     * @param dfUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    @Override
    public List<IndexDto> loadIndexes(Long dfUid,
                                      UserInfo operationUserInfo) throws ServiceException {
        return this.dfConfHandler.loadIndexes(dfUid, operationUserInfo);
    }

    @Override
    public List<DfSimpleDto> listDfByCreatedBy(String username,
                                             UserInfo operationUserInfo) throws ServiceException {
        return this.dfDefHandler.listDfByCreatedBy(username, operationUserInfo);
    }

    @Override
    public List<DfSimpleDto> listAllDf(UserInfo operationUserInfo) throws ServiceException {
        return this.dfDefHandler.listAllDf(operationUserInfo);
    }

    @Override
    public List<DfSimpleDto> listDf(List<Long> listOfDfUid) throws ServiceException {
        return this.dfDefHandler.listDf(listOfDfUid);
    }
}
