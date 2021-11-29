package io.g740.pigeon.biz.service.interfaces.openapi;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author bbottong
 */
public interface OpenApiService {
    /**
     * 分页列出指定App的所有Data Facet (DF)
     * @param appKey
     * @param pageable
     * @return
     * @throws ServiceException
     */
    PageResult<DfSimpleDto> pagedListAllDfForApp(
            String appKey, Pageable pageable) throws ServiceException;

    /**
     * 列出指定App的所有Data Facet (DF)
     *
     * @param appKey
     * @return
     * @throws ServiceException
     */
    List<DfSimpleDto> listAllDfForApp(
            String appKey) throws ServiceException;
}
