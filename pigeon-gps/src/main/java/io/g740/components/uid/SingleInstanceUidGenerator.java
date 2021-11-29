package io.g740.components.uid;

import io.g740.commons.types.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UID生成器，适用于：
 *  1) 一种服务，一个实例，一个数据库；
 */
@Manager
public class SingleInstanceUidGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleInstanceUidGenerator.class);

    /**
     * 1st dimension - domain, 1st dimension - timestamp in milliseconds
     */
    private final Map<String, Map<Long, Long>> currentMillisecondsWatermark
            = new ConcurrentHashMap<>(100);

    /**
     * 创始时间戳：2021-01-01 00:00:00 的时间戳
     */
    private static final Long FOUNDING_TIME_IN_SECONDS = 1609430400L;

    private static final Long CAPACITY = 999999L;

    /**
     * 根据当前时间戳顺序生成uid
     * <p>
     * 一秒内允许CAPACITY个不重复值
     *
     * @return
     */
    public synchronized Long generateUid(String domain) {
        Date now = new Date();
        // 当前时间戳 - 创始时间戳
        Long nowTimeInSeconds = now.getTime() / 1000 - FOUNDING_TIME_IN_SECONDS;
        Long newWatermark = 1L;

        if (!this.currentMillisecondsWatermark.containsKey(domain)) {
            this.currentMillisecondsWatermark.put(domain, new ConcurrentHashMap<>(100));
        } else {
            Long lastWatermark = this.currentMillisecondsWatermark.get(domain).get(nowTimeInSeconds);
            if (lastWatermark != null) {
                newWatermark = lastWatermark + 1L;

                // 1 second内超出负载，借用下一个second
                if (newWatermark > CAPACITY) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOGGER.info(e.getMessage(), e);
                    } finally {
                        Date newNow = new Date();
                        Long newNowTimeInMilliseconds = newNow.getTime() / 1000 - FOUNDING_TIME_IN_SECONDS;
                        if (newNowTimeInMilliseconds > nowTimeInSeconds) {
                            nowTimeInSeconds = newNowTimeInMilliseconds;
                            newWatermark = 1L;
                        } else {
                            throw new RuntimeException("[tinyid]1 second exceeds " + CAPACITY + " uid, " +
                                    "and cannot borrow from next second");
                        }
                    }
                }

                // 清除历史秒水位
                if (this.currentMillisecondsWatermark.get(domain).size() > 100) {
                    this.currentMillisecondsWatermark.get(domain).clear();
                }
            }
        }

        Long result = nowTimeInSeconds * (CAPACITY + 1) + newWatermark;
        this.currentMillisecondsWatermark.get(domain).put(nowTimeInSeconds, newWatermark);
        return result;
    }
}
