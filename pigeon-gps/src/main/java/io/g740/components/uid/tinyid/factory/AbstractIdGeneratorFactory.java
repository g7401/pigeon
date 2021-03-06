package io.g740.components.uid.tinyid.factory;


import io.g740.components.uid.tinyid.generator.IdGenerator;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author du_imba
 */
public abstract class AbstractIdGeneratorFactory implements IdGeneratorFactory {

    private static final ConcurrentHashMap<String, IdGenerator> generators = new ConcurrentHashMap<>();

    @Override
    public IdGenerator getIdGenerator(String bizType) {
        if (generators.containsKey(bizType)) {
            return generators.get(bizType);
        }
        synchronized (this) {
            if (generators.containsKey(bizType)) {
                return generators.get(bizType);
            }
            IdGenerator idGenerator = createIdGenerator(bizType);
            generators.put(bizType, idGenerator);
            return idGenerator;
        }
    }

    /**
     * 根据bizType创建id生成器
     *
     * @param bizType
     * @return
     */
    protected abstract IdGenerator createIdGenerator(String bizType);
}
