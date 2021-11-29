package io.g740.components.uid.tinyid.factory.impl;

import io.g740.components.uid.tinyid.factory.AbstractIdGeneratorFactory;
import io.g740.components.uid.tinyid.generator.IdGenerator;
import io.g740.components.uid.tinyid.generator.impl.CachedIdGenerator;
import io.g740.components.uid.tinyid.service.SegmentIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author du_imba
 */
@Component
public class IdGeneratorFactoryServer extends AbstractIdGeneratorFactory {

    private static final Logger logger = LoggerFactory.getLogger(CachedIdGenerator.class);
    @Autowired
    private SegmentIdService tinyIdService;

    @Override
    public IdGenerator createIdGenerator(String bizType) {
        logger.info("[tinyid]createIdGenerator :{}", bizType);
        return new CachedIdGenerator(bizType, tinyIdService);
    }
}
