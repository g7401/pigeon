package io.g740.components.dlock;

import io.g740.commons.types.TwoTuple;
import io.g740.pigeon.init.ShutdownCleaner;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bbottong
 */
@Service
public class DlockServiceImpl implements DlockService, ShutdownCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DlockServiceImpl.class);

    @Autowired
    private DlockRepository dlockRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * l1 key - bizType,
     * l2 key - serialNo,
     * DateTime - lock expired at
     */
    private Map<String, Map<String, TwoTuple<Connection, DateTime>>> usage = new ConcurrentHashMap<>();

    private static final String AUDIT_TIMEOUT_CRON_EXPRESSION = "0 0/1 * * * ?";

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String getLock(String bizType) throws Exception {
        LOGGER.info("[dlock]getLock start::{}", bizType);

        Connection connection = null;

        try {
            connection = this.jdbcTemplate.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new Exception("failed to get lock::" + "biz_type:" + bizType, e);
        }

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            closeConnection(connection, bizType);

            throw new Exception("failed to get lock::" + "biz_type:" + bizType, e);
        }

        DlockDo dlockDo = null;

        try {
            dlockDo = this.dlockRepository.selectForUpdate(bizType, connection);
        } catch (Exception e) {
            closeConnection(connection, bizType);

            throw new Exception("failed to get lock::" + "biz_type:" + bizType, e);
        }

        if (dlockDo != null) {
            LOGGER.info("[dlock]getLock done::{}", bizType);

            String serialNo = UUID.randomUUID().toString();

            if (this.usage.get(bizType) == null) {
                this.usage.put(bizType, new ConcurrentHashMap<>());
            }
            this.usage.get(bizType).put(serialNo, new TwoTuple<>(connection, DateTime.now().plusMinutes(dlockDo.getTimeoutInMinutes())));

            // 不要 close connection，释放锁时才 close connection

            return serialNo;
        } else {
            LOGGER.info("[dlock]getLock conflict::{}", bizType);

            closeConnection(connection, bizType);

            throw new Exception("failed to get lock");
        }
    }

    private void closeConnection(Connection connection, String bizType) {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("[dlock]failed to close connection::" + "biz_type:" + bizType, e);
        }
    }

    @Override
    public void releaseLock(String bizType, String serialNo) {
        if (this.usage.containsKey(bizType)) {
            if (this.usage.get(bizType).containsKey(serialNo)) {
                TwoTuple<Connection, DateTime> tuple = this.usage.get(bizType).get(serialNo);
                Connection connection = tuple.f;
                try {
                    this.dlockRepository.update(bizType, connection);
                    connection.commit();
                } catch (SQLException e) {
                    LOGGER.error("[dlock]failed to commit connection::"
                                    + "biz_type:" + bizType
                                    + ", serial_no:" + serialNo,
                            e);
                } finally {
                    closeConnection(connection, bizType);
                }
                this.usage.get(bizType).remove(serialNo);
                LOGGER.info("[dlock]releaseLock done::{}, {}", bizType, serialNo);
            } else {
                LOGGER.warn("[dlock]releaseLock ignored::{}, {}, cannot find serial_no", bizType, serialNo);
            }
        } else {
            LOGGER.warn("[dlock]releaseLock ignored::{}, {}, cannot find biz_type", bizType, serialNo);
        }
    }

    @Scheduled(cron = AUDIT_TIMEOUT_CRON_EXPRESSION)
    public void auditTimeout() {
        LOGGER.info("[dlock]auditTimeout begin");

        DateTime nowDateTime = DateTime.now();

        this.usage.forEach((bizType, value) -> {
            value.forEach((serialNo, tuple) -> {
                DateTime expiredAt = tuple.s;

                if (expiredAt.isBefore(nowDateTime)) {
                    // yes, timeout
                    LOGGER.info("[dlock]auditTimeout, found expired lock::"
                            + "biz_type:" + bizType
                            + ", serial_no:" + serialNo
                            + ", expired_at:" + expiredAt);

                    // release lock
                    releaseLock(bizType, serialNo);
                }
            });
        });

        LOGGER.info("[dlock]auditTimeout end");
    }

    @Override
    public void shutdown() throws Exception {
        LOGGER.info("[dlock]shutdown begin");

        this.usage.forEach((bizType, value) -> {
            value.forEach((serialNo, tuple) -> {
                // release lock
                releaseLock(bizType, serialNo);
            });
        });

        LOGGER.info("[dlock]shutdown end");
    }
}
