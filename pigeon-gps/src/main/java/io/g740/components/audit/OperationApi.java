package io.g740.components.audit;

import io.g740.commons.api.Response;
import io.g740.commons.types.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bbottong
 */
@Api(tags = "Operations APIs")
@RestController
@RequestMapping(value = "/operations")
public class OperationApi {
    @Autowired
    private OperationService operationService;

    @ApiOperation("分页查询Operations")
    @GetMapping("/paging_query")
    @ResponseBody
    public Response<PageResult<OperationDto>> pagingQueryOperations(
            @RequestParam(name = "request_id", required = false) String requestId,
            @RequestParam(name = "request_uri", required = false) String requestUri,
            @RequestParam(name = "parameters", required = false) String parameters,
            Pageable pageable) throws Exception {
        return Response.buildSuccess(
                this.operationService.pagingQueryOperation(requestId, requestUri, parameters, pageable));
    }

    @ApiOperation("获取指定request_id的所有Operations，且按Operation发生的时间先后顺序排列")
    @GetMapping("/details")
    @ResponseBody
    public Response<OperationDetailsDto> getDetailsOfOperationByRequestId(
            @RequestParam(name = "request_id") String requestId) throws Exception {
        return Response.buildSuccess(
                this.operationService.getDetailsOfOperationByRequestId(requestId));
    }
}
