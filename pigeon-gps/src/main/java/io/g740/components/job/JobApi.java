package io.g740.components.job;

import io.g740.commons.api.Response;
import io.g740.pigeon.biz.service.interfaces.account.AccountService;
import io.g740.pigeon.biz.service.interfaces.oauth.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author bbottong
 */
@Api(tags = "Job APIs")
@RestController
@RequestMapping(value = "/general")
public class JobApi {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JobHandler jobHandler;

    @ApiOperation("获取job失败原因")
    @GetMapping("/job_failed_reasons")
    @ResponseBody
    public Response<JobFailedReasonDto> getJobFailedReason(
            @RequestParam(name = "job_category") String jobCategory,
            @RequestParam(name = "job_uid") Long jobUid) throws Exception {
        return Response.buildSuccess(
                this.jobHandler.getJobFailedReason(jobCategory, jobUid));
    }
}
