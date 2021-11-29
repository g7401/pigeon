package io.g740.components.uid.tinyid.generator.impl;


import io.g740.components.uid.tinyid.entity.Result;
import io.g740.components.uid.tinyid.entity.ResultCode;
import io.g740.components.uid.tinyid.entity.SegmentId;
import io.g740.components.uid.tinyid.exception.TinyIdSysException;
import io.g740.components.uid.tinyid.generator.IdGenerator;
import io.g740.components.uid.tinyid.service.SegmentIdService;
import io.g740.components.uid.tinyid.service.impl.DbSegmentIdServiceImpl;
import io.g740.components.uid.tinyid.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author du_imba
 */
public class CachedIdGenerator implements IdGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CachedIdGenerator.class);

    protected String bizType;
    protected SegmentIdService segmentIdService;
    protected volatile SegmentId current;
    protected volatile SegmentId next;
    private volatile boolean isLoadingNext;
    private final Object lock = new Object();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("tinyid"));

    public CachedIdGenerator(String bizType, SegmentIdService segmentIdService) {
        this.bizType = bizType;
        this.segmentIdService = segmentIdService;
        loadCurrent();
    }

    public synchronized void loadCurrent() {
        if (current == null || !current.useful()) {
            if (next == null) {
                SegmentId segmentId = querySegmentId();

                if (current != null) {
                    if (segmentId.getMaxId() <= current.getMaxId()) {
                        throw new TinyIdSysException("[tinyid]error query segmentId::" + "got illegal segment," +
                                "current:" + current
                                + ";new:" + segmentId);
                    }
                }

                this.current = segmentId;
            } else {
                current = next;
                next = null;
            }
        }
    }

    private SegmentId querySegmentId() throws TinyIdSysException {
        String message = null;
        try {
            SegmentId segmentId = segmentIdService.getNextSegmentId(bizType);
            if (segmentId != null) {
                return segmentId;
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        throw new TinyIdSysException("[tinyid]error query segmentId: " + message);
    }

    public void loadNext() {
        if (next == null && !isLoadingNext) {
            synchronized (lock) {
                if (next == null && !isLoadingNext) {
                    logger.info("[tinyid]loadNext start::" + bizType
                            + ",current=" + current);

                    isLoadingNext = true;
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 无论获取下个segmentId成功与否，都要将isLoadingNext赋值为false
                                next = querySegmentId();
                            } catch (TinyIdSysException e) {
                                logger.error("[tinyid]failed to loadNext::" + bizType
                                        + ",current=" + current
                                        + ",next=" + next
                                        + ",isLoadingNext=" + isLoadingNext, e);
                            } finally {
                                isLoadingNext = false;
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public Long nextId() {
        while (true) {
            if (current == null) {
                loadCurrent();
                continue;
            }
            Result result = current.nextId();
            if (result.getCode() == ResultCode.OVER) {
                loadCurrent();
            } else {
                if (result.getCode() == ResultCode.LOADING) {
                    loadNext();
                }
                return result.getId();
            }
        }
    }

    @Override
    public List<Long> nextId(Integer batchSize) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            Long id = nextId();
            ids.add(id);
        }
        return ids;
    }

}
