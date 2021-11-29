package io.g740.components.audit;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.PageResult;
import org.springframework.data.domain.Pageable;

public interface OperationService {
    void createOperation(
            String requestId,
            String requestUri,
            String parameters) throws ServiceException;

    void createOperationTask(
            String requestId,
            String task,
            String input,
            String output) throws ServiceException;

    /**
     * 分页查询
     *
     * @param requestId
     * @param requestUri
     * @param parameters
     * @param pageable
     * @return
     * @throws ServiceException
     */
    PageResult<OperationDto> pagingQueryOperation(
            String requestId,
            String requestUri,
            String parameters,
            Pageable pageable) throws ServiceException;


    /**
     * 获取指定request_id的所有Operations，且按Operation发生的时间先后顺序排列
     *
     * @param requestId
     * @return
     * @throws ServiceException
     */
    OperationDetailsDto getDetailsOfOperationByRequestId(
            String requestId) throws ServiceException;
}
