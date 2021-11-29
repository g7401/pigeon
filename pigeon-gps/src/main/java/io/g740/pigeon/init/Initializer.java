package io.g740.pigeon.init;

import io.g740.pigeon.base.manager.LogicInitializationManager;
import io.g740.pigeon.base.manager.EnvironmentAssuranceManager;
import io.g740.pigeon.base.manager.DataInitializationManager;
import io.g740.pigeon.base.manager.SchedulingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author bbottong
 */
@Component
public class Initializer implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EnvironmentAssuranceManager environmentAssuranceManager;

    @Autowired
    private SchedulingManager schedulingManager;

    @Autowired
    private DataInitializationManager dataInitializationManager;

    @Autowired
    private LogicInitializationManager logicInitializationManager;

    @Value("${spring.profiles.active}")
    private String runningMode;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            LOGGER.info("Start to init infrastructure layer");
            initInfrastructureLayer();
            LOGGER.info("Done to init infrastructure layer");
        } catch (Exception e) {
            LOGGER.error("Failed to init infrastructure layer");
            SpringApplication.exit(this.applicationContext, () -> 101);
            return;
        }

        try {
            LOGGER.info("Start to init application layer");
            initApplicationLayer();
            LOGGER.info("Done to init application layer");
        } catch (Exception e) {
            LOGGER.error("Failed to init application layer");
            SpringApplication.exit(this.applicationContext, () -> 102);
            return;
        }
    }

    /**
     * 初始化 Infrastructure Layer
     *
     * @throws Exception
     */
    private void initInfrastructureLayer() throws Exception {
        try {
            LOGGER.info("Start to execute environmentAssuranceManager");
            this.environmentAssuranceManager.execute();
            LOGGER.info("Done to execute environmentAssuranceManager");
        } catch (Exception e) {
            LOGGER.error("Failed to execute environmentAssuranceManager", e);
            throw e;
        }
    }

    /**
     * 初始化 Application Layer
     *
     * @throws Exception
     */
    private void initApplicationLayer() throws Exception {
        try {
            LOGGER.info("Start to execute dataInitializationManager");
            this.dataInitializationManager.execute();
            LOGGER.info("Done to execute dataInitializationManager");
        } catch (Exception e) {
            LOGGER.error("Failed to execute dataInitializationManager", e);
            throw e;
        }

        try {
            LOGGER.info("Start to execute logicInitializationManager");
            this.logicInitializationManager.execute();
            LOGGER.info("Done to execute logicInitializationManager");
        } catch (Exception e) {
            LOGGER.error("Failed to execute logicInitializationManager", e);
            throw e;
        }

        try {
            LOGGER.info("Start to execute schedulingManager");
            this.schedulingManager.execute();
            LOGGER.info("Done to execute schedulingManager");
        } catch (Exception e) {
            LOGGER.error("Failed to execute schedulingManager", e);
            throw e;
        }
    }
}