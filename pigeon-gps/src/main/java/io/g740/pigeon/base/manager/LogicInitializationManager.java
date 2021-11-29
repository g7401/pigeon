package io.g740.pigeon.base.manager;

import io.g740.pigeon.biz.service.handler.admin.DefaultsBuildProcessHandler;
import io.g740.pigeon.biz.service.handler.admin.DictionaryBuildProcessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 初始化逻辑
 *
 * @author bbottong
 */
@Component
public class LogicInitializationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogicInitializationManager.class);

    @Autowired
    private DictionaryBuildProcessHandler dictionaryBuildProcessHandler;

    @Autowired
    private DefaultsBuildProcessHandler defaultsBuildProcessHandler;

    public void execute() throws Exception {
        LOGGER.info("dictionary build init register scheduling");
        this.dictionaryBuildProcessHandler.initRegisterScheduling();

        LOGGER.info("defaults build init register scheduling");
        this.defaultsBuildProcessHandler.initRegisterScheduling();

    }

}
