package io.g740.pigeon.biz.persistence.jdbc;

import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.object.dto.export.AsyncExportJobDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 * <p>
 * 原生SQL存取
 */
public interface NativeRepository {
    /**
     * 查询异步导出工作
     *
     * @param parameterMap
     * @param pageable
     * @param operationUserInfo
     * @return
     * @throws Exception
     */
    PageResult<AsyncExportJobDto> queryAsyncExportJob(Map<String, String[]> parameterMap,
                                                      Pageable pageable,
                                                      UserInfo operationUserInfo) throws Exception;

    /**
     * 按tag(s)和df name分页查找df
     *
     * @param tags
     * @param name
     * @param listOfAvailableDfUid 允许查找DF的最大范围
     * @param createBy             如果有值，可以不受listOfAvailableDfUid的限制查看createBy所创建的DF
     * @param pageable
     * @return
     * @throws Exception
     */
    PageResult<DfSimpleDto> queryDfByTagsAndDfNameWithinLimitRange(List<String> tags,
                                                                   String name,
                                                                   List<Long> listOfAvailableDfUid,
                                                                   String createBy,
                                                                   Pageable pageable) throws Exception;

    /**
     * 在指定表中查询最大的uid字段值
     *
     * @param tableName
     * @return
     * @throws Exception
     */
    Long queryMaximumUid(String tableName) throws Exception;
}
