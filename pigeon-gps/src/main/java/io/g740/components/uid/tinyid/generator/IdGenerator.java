package io.g740.components.uid.tinyid.generator;

import java.util.List;

/**
 * @author du_imba
 */
public interface IdGenerator {
    /**
     * get next id
     * @return
     */
    Long nextId();

    /**
     * get next id batch
     * @param batchSize
     * @return
     */
    List<Long> nextId(Integer batchSize);
}
