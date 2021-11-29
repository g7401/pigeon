package io.g740.components.dlock;

public interface DlockService {
    String getLock(String bizType) throws Exception;

    void releaseLock(String bizType, String serialNo);
}
