package io.g740.pigeon.biz.service.impl.openapi;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.service.handler.openapi.OpenApiHandler;
import io.g740.pigeon.biz.service.interfaces.openapi.OpenApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bbottong
 */
@Service
public class OpenApiServiceImpl implements OpenApiService {

    @Autowired
    private OpenApiHandler openApiHandler;

    @Override
    public PageResult<DfSimpleDto> pagedListAllDfForApp(
            String appKey, Pageable pageable) throws ServiceException {
        return this.openApiHandler.pagedListAllDfForApp(appKey, pageable);
    }

    @Override
    public List<DfSimpleDto> listAllDfForApp(String appKey) throws ServiceException {
        return this.openApiHandler.listAllDfForApp(appKey);
    }
}
