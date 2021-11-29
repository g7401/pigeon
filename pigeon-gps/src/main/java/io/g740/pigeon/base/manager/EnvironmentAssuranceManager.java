package io.g740.pigeon.base.manager;

import io.g740.components.dlock.DlockService;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 运行环境保证
 */
@Component
public class EnvironmentAssuranceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentAssuranceManager.class);

    /**
     * 几个通用的目录，要求系统启动时创建
     */
    @Value("${application.dir.general.project}")
    private String dirGeneralProject;
    @Value("${application.dir.general.project.download}")
    private String dirGeneralProjectDownload;
    @Value("${application.dir.general.project.upload}")
    private String dirGeneralProjectUpload;
    @Value("${application.dir.general.project.export}")
    private String dirGeneralProjectExport;
    @Value("${application.dir.general.project.logs}")
    private String dirGeneralProjectLogs;
    @Value("${application.dir.general.project.plugins}")
    private String dirGeneralProjectPlugins;
    @Value("${application.dir.general.project.extract}")
    private String dirGeneralProjectExtract;

    @Value("${application.audit.delete-local-files-exists-days}")
    private Integer auditDeleteLocalFilesExistsDays;

    public static final String CLEANUP_LOCAL_FILES_CRON_EXPRESSION = "0 15 0 * * ?";

    @Autowired
    private DlockService dlockService;

    public void execute() throws Exception {
        try {
            LOGGER.info("Start to Init local directories");
            initLocalDirectories();
            LOGGER.info("Done to init local directories");
        } catch (Exception e) {
            LOGGER.error("Failed to init local directories");
            throw e;
        }
    }

    private void initLocalDirectories() throws Exception {
        Files.createDirectories(Paths.get(this.dirGeneralProject));
        Files.createDirectories(Paths.get(this.dirGeneralProjectDownload));
        Files.createDirectories(Paths.get(this.dirGeneralProjectUpload));
        Files.createDirectories(Paths.get(this.dirGeneralProjectExport));
        Files.createDirectories(Paths.get(this.dirGeneralProjectLogs));
        Files.createDirectories(Paths.get(this.dirGeneralProjectPlugins));
        Files.createDirectories(Paths.get(this.dirGeneralProjectExtract));
    }

    /**
     * 定时清理本地临时文件
     * 不需要分布式锁支持
     */
    @Scheduled(cron = CLEANUP_LOCAL_FILES_CRON_EXPRESSION)
    public void cleanupLocalFiles() {
        cleanupDirectory(this.dirGeneralProjectDownload);
        cleanupDirectory(this.dirGeneralProjectUpload);
        cleanupDirectory(this.dirGeneralProjectExport);
        cleanupDirectory(this.dirGeneralProjectExtract);
    }

    private void cleanupDirectory(String directory) {
        LOGGER.info("[AUDIT100]cleanup local files in directory: {}", directory);

        DateTime nowDateTime = new DateTime(new java.util.Date());
        DateTime thresholdDateTime = nowDateTime.minusDays(this.auditDeleteLocalFilesExistsDays);
        long threshold = thresholdDateTime.toDate().getTime();

        List<Path> result = null;
        try {
            Stream<Path> walk = Files.walk(Paths.get(directory));
            result = walk.filter(Files::isRegularFile)
                    .filter(path -> (path.toFile().lastModified() < threshold))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("failed to walk directory: " + directory + ". " + e.getMessage(), e);
        }

        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(path -> {
                try {
                    Files.delete(path);
                    LOGGER.warn("[AUDIT101]done deleting expired local file: " + path);
                } catch (IOException e) {
                    LOGGER.warn("[AUDIT102]failed deleting expired local file: " + path);
                }
            });
        }
    }
}
