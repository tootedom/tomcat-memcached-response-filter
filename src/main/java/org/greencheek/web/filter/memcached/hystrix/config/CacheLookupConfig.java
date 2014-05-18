package org.greencheek.web.filter.memcached.hystrix.config;

/**
 * Created by dominictootell on 18/05/2014.
 */
public class CacheLookupConfig {

    private final boolean useThreadPool;
    private final int semaphoreSize;
    private final int lookupTimeoutInMillis;
    private final int batchingTimeInMillis;
    private final int batchingMaxSize;
    private final int threadPoolSize;
    private final int threadPoolQueueSize;

    public CacheLookupConfig(boolean useThreadPool, int semaphoreSize,
                             int lookupTimeout, int batchingTime,
                             int batchingMaxSize,int threadPoolSize,
                             int threadPoolQueueSize) {
        this.useThreadPool = useThreadPool;
        this.semaphoreSize = semaphoreSize;
        this.lookupTimeoutInMillis = lookupTimeout;
        this.batchingTimeInMillis = batchingTime;
        this.batchingMaxSize = batchingMaxSize;
        this.threadPoolQueueSize = threadPoolQueueSize;
        this.threadPoolSize = threadPoolSize;

    }

    public boolean isUseThreadPool() {
        return useThreadPool;
    }

    public int getSemaphoreSize() {
        return semaphoreSize;
    }

    public int getLookupTimeoutInMillis() {
        return lookupTimeoutInMillis;
    }

    public int getBatchingTimeInMillis() {
        return batchingTimeInMillis;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getThreadPoolQueueSize() {
        return threadPoolQueueSize;
    }

    public int getBatchingMaxSize() {
        return batchingMaxSize;
    }
}
