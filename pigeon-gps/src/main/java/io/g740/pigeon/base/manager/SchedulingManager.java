package io.g740.pigeon.base.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/**
 * 统一调度
 *
 * @author bbottong
 */
@Component
public class SchedulingManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingManager.class);

    // 10 seconds
    private static final long TEN_SECONDS_IN_MS = 10000;
    // 15 seconds
    private static final long FIFTEEN_SECONDS_IN_MS = 15000;
    // 30 seconds
    private static final long THIRTY_SECONDS_IN_MS = 30000;
    // 1 minutes
    private static final long ONE_MINUTE_IN_MS = 60000;
    private static final long ONE_MINUTE_IN_SECONDS = 60;
    // 5 minutes
    private static final long FIVE_MINUTES_IN_MS = 300000;
    private static final long FIVE_MINUTES_IN_SECONDS = 300;
    // 10 minutes
    private static final long TEN_MINUTES_IN_MS = 600000;
    private static final long TEN_MINUTES_IN_SECONDS = 600;
    // 15 minutes
    private static final long FIFTEEN_MINUTES_IN_MS = 900000;
    private static final long FIFTEEN_MINUTES_IN_SECONDS = 900;

    @Autowired
    private TaskScheduler taskScheduler;

    public void execute() throws Exception {

    }

}
