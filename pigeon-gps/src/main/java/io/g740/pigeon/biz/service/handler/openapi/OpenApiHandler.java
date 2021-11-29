package io.g740.pigeon.biz.service.handler.openapi;

import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.object.entity.app.AppDfDo;
import io.g740.pigeon.biz.object.entity.app.AppDo;
import io.g740.pigeon.biz.persistence.jpa.app.AppDfRepository;
import io.g740.pigeon.biz.persistence.jpa.app.AppRepository;
import io.g740.pigeon.biz.service.handler.deployment.DfDeploymentQueryHandler;
import io.g740.pigeon.biz.service.interfaces.admin.DfService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bbottong
 */
@Handler
public class OpenApiHandler {
    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppDfRepository appDfRepository;

    @Autowired
    private DfDeploymentQueryHandler dfDeploymentQueryHandler;

    @Autowired
    private DfService dfService;

    public PageResult<DfSimpleDto> pagedListAllDfForApp(
            String appKey, Pageable pageable) throws ServiceException {
        AppDo appDo = this.appRepository.findByAppKey(appKey);
        if (appDo == null) {
            throw new ResourceNotFoundException(AppDo.RESOURCE_NAME + ":" + appKey);
        }

        PageResult<DfSimpleDto> pageResult = new PageResult<>();

        Page<AppDfDo> pageOfAppDfDo = this.appDfRepository.findByAppUid(appDo.getUid(), pageable);
        pageResult.setNumberOfElements(pageOfAppDfDo.getNumberOfElements());
        pageResult.setPageNumber(pageable.getPageNumber());
        pageResult.setPageSize(pageable.getPageSize());
        pageResult.setTotalElements(pageOfAppDfDo.getTotalElements());
        pageResult.setTotalPages(pageOfAppDfDo.getTotalPages());

        if (!pageOfAppDfDo.isEmpty()) {
            pageResult.setContent(new ArrayList<>());

            List<Long> listOfDfUid = new ArrayList<>();
            for (AppDfDo appDfDo : pageOfAppDfDo.getContent()) {
                listOfDfUid.add(appDfDo.getDfUid());
            }
            List<DfSimpleDto> listOfDfSimpleDto = this.dfService.listDf(listOfDfUid);
            pageResult.getContent().addAll(listOfDfSimpleDto);
        }

        return pageResult;
    }

    public List<DfSimpleDto> listAllDfForApp(String appKey) throws ServiceException {
        AppDo appDo = this.appRepository.findByAppKey(appKey);
        if (appDo == null) {
            throw new ResourceNotFoundException(AppDo.RESOURCE_NAME + ":" + appKey);
        }

        List<Long> listOfDfUid = this.appDfRepository.findDfUidByAppUid(appDo.getUid());
        if (CollectionUtils.isNotEmpty(listOfDfUid)) {
            List<DfSimpleDto> listOfDfSimpleDto = this.dfService.listDf(listOfDfUid);
            return listOfDfSimpleDto;
        }

        return null;
    }
}
