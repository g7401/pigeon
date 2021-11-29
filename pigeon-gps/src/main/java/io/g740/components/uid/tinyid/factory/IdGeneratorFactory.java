package io.g740.components.uid.tinyid.factory;


import io.g740.components.uid.tinyid.generator.IdGenerator;

/**
 * @author du_imba
 */
public interface IdGeneratorFactory {
    /**
     * 根据bizType创建id生成器
     * @param bizType
     * @return
     */
    IdGenerator getIdGenerator(String bizType);
}
