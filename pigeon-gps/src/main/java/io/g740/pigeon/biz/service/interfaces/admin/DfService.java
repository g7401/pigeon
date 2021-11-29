package io.g740.pigeon.biz.service.interfaces.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.ds.IndexDto;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author bbottong
 * <p>
 * Data Facet (DF)
 */
public interface DfService {
    /**
     * 创建或替换DF
     *
     * @param createOrReplaceDfDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DfDto createOrReplaceDf(CreateOrReplaceDfDto createOrReplaceDfDto,
                            UserInfo operationUserInfo) throws ServiceException;

    /**
     * 删除DF
     *
     * @param uid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void deleteDf(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 根据uid获取DF
     *
     * @param uid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DfDto getDfByUid(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 根据key获取DF
     *
     * @param key
     * @return
     * @throws ServiceException
     */
    DfSimpleDto getDfByKey(String key, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有Data Objects and Data Facets
     *
     * @param showDfOnly        true - 只显示Data Facets
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    SimpleTreeNode listAllDataObjectsWithOrWithoutDf(Boolean showDfOnly,
                                                     UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取DF的配置，包括基本配置和高级配置
     *
     * @param uid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DfConfDto getDfConf(Long uid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取DF的配置，只包括基本配置
     *
     * @param dfUid
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    PageResult<DfConfDataFieldDto> getDfConfBasic(
            Long dfUid, Pageable pageable, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取DF的配置，只包括高级配置
     *
     * @param dfUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DfConfAdvancedDto getDfConfAdvanced(
            Long dfUid, UserInfo operationUserInfo) throws ServiceException;

    /**
     * 创建或者更新DF的配置，包括基本配置和高级配置
     *
     * @param createOrReplaceDfConfDto
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DfConfDto createOrReplaceDfConf(CreateOrReplaceDfConfDto createOrReplaceDfConfDto,
                                    UserInfo operationUserInfo) throws ServiceException;

    /**
     * 刷新DF的可用字段，即从Data Facet所依存的表/视图重新加载字段
     *
     * @param dfUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    DfRefreshResultDto refreshDfAvailableDataFields(Long dfUid,
                                                    UserInfo operationUserInfo) throws ServiceException;

    /**
     * 将Source DF的CONF复制给Destination DF
     *
     * @param sourceDfUid
     * @param destinationDfUid
     * @param operationUserInfo
     * @throws ServiceException
     */
    void replicateDfConf(Long sourceDfUid, Long destinationDfUid,
                         UserInfo operationUserInfo) throws ServiceException;

    /**
     * 加载指定Data Facet所依存的表的索引
     *
     * @param dfUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<IndexDto> loadIndexes(Long dfUid,
                               UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出指定用户创建的DF
     *
     * @param username
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DfSimpleDto> listDfByCreatedBy(String username,
                                        UserInfo operationUserInfo) throws ServiceException;

    /**
     * 列出所有DF
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    List<DfSimpleDto> listAllDf(UserInfo operationUserInfo) throws ServiceException;

    /**
     * 获取指定的一批df
     *
     * @param listOfDfUid
     * @return
     * @throws ServiceException
     */
    List<DfSimpleDto> listDf(List<Long> listOfDfUid) throws ServiceException;
}
