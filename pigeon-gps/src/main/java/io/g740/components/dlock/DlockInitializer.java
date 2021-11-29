package io.g740.components.dlock;

import io.g740.App;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import sun.reflect.MethodAccessor;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

@Component
public class DlockInitializer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DlockInitializer.class);

    @Autowired
    private DlockRepository dlockRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections reflections =
                new Reflections(
                        new ConfigurationBuilder()
                                .setUrls(ClasspathHelper.forPackage(App.class.getPackage().getName()))
                                .setScanners(Scanners.MethodsAnnotated));
        final Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(Dlock.class);
        for (Method method : methodsAnnotatedWith) {
            final Dlock dlock = method.getAnnotation(Dlock.class);
            final String bizType = dlock.bizType();
            final int timeoutInMinutes = dlock.timeoutInMinutes();

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

            if (dlockDo == null) {
                dlockDo = new DlockDo();
                dlockDo.setCreateTimestamp(new java.util.Date());
                dlockDo.setLastUpdateTimestamp(new java.util.Date());
                dlockDo.setBizType(bizType);
                dlockDo.setTimeoutInMinutes(timeoutInMinutes);

                boolean ret = this.dlockRepository.create(dlockDo, connection);
                if (!ret) {
                    throw new Exception("failed to create lock::" + "biz_type:" + bizType);
                }
            }

            try {
                connection.commit();
            } catch (Exception e) {
                LOGGER.error("failed to commit connection::" + "biz_type:" + bizType);
            }

            closeConnection(connection, bizType);
        }
    }

    private void closeConnection(Connection connection, String bizType) {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("[dlock]failed to close connection::" + "biz_type:" + bizType, e);
        }
    }
}
