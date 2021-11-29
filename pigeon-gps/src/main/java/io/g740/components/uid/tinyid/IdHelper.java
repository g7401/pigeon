package io.g740.components.uid.tinyid;

import io.g740.App;
import io.g740.components.uid.tinyid.dao.TinyIdInfoDAO;
import io.g740.components.uid.tinyid.dao.entity.TinyIdInfo;
import io.g740.components.uid.tinyid.factory.impl.IdGeneratorFactoryServer;
import io.g740.components.uid.tinyid.generator.IdGenerator;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

/**
 * @author: zxw
 * @version: v1.0
 * @description:
 */
@Component
public class IdHelper implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdHelper.class);

    @Autowired
    private IdGeneratorFactoryServer idGeneratorFactoryServer;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TinyIdInfoDAO tinyIdInfoDao;

    public Long getNextId(String bizType) {
        final IdGenerator idGenerator = idGeneratorFactoryServer.getIdGenerator(bizType);
        return idGenerator.nextId();
    }

    public String getNextIdOfStr(String bizType) {
        final IdGenerator idGenerator = idGeneratorFactoryServer.getIdGenerator(bizType);
        return idGenerator.nextId().toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections reflections = new Reflections(App.class.getPackage().getName());
        final Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(TinyId.class);
        for (Class<?> clazz : typesAnnotatedWith) {
            final TinyId tinyId = clazz.getAnnotation(TinyId.class);
            final String bizType = tinyId.bizType();
            final long beginId = tinyId.beginId();
            final int delta = tinyId.delta();
            final long maxId = tinyId.maxId();
            final int remainder = tinyId.remainder();
            final int step = tinyId.step();
            TinyIdInfo tinyIdInfo = tinyIdInfoDao.queryByBizType(bizType);
            if (tinyIdInfo == null) {
                tinyIdInfo = new TinyIdInfo();
                tinyIdInfo.setBeginId(beginId);
                tinyIdInfo.setBizType(bizType);
                tinyIdInfo.setCreateTime(new Date());
                tinyIdInfo.setDelta(delta);
                tinyIdInfo.setMaxId(maxId);
                tinyIdInfo.setRemainder(remainder);
                tinyIdInfo.setStep(step);
                tinyIdInfo.setUpdateTime(new Date());
                tinyIdInfo.setVersion(0L);
                tinyIdInfoDao.create(tinyIdInfo);
            }
        }
    }

}
