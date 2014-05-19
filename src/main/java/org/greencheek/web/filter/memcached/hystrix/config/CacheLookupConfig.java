package org.greencheek.web.filter.memcached.hystrix.config;

import com.netflix.hystrix.*;

/**
 * Created by dominictootell on 18/05/2014.
 */
public class CacheLookupConfig {

    private final boolean useThreadPool;
    private final int semaphoreSize;
    private final int lookupTimeoutInMillis;
    private final boolean batchingEnabled;
    private final int batchingTimeInMillis;
    private final int batchingMaxSize;
    private final int threadPoolSize;
    private final int threadPoolQueueSize;

    public CacheLookupConfig(boolean useThreadPool, int semaphoreSize,
                             int lookupTimeout, boolean batchingEnabled,
                             int batchingTime,int batchingMaxSize,
                             int threadPoolSize,int threadPoolQueueSize) {
        this.useThreadPool = useThreadPool;
        this.semaphoreSize = semaphoreSize;
        this.lookupTimeoutInMillis = lookupTimeout;
        this.batchingEnabled = batchingEnabled;
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

    public boolean isBatchingEnabled() {
        return batchingEnabled;
    }

    public static HystrixCollapser.Setter createCollapserSettings(CacheLookupConfig cacheLookupConfig) {
        return HystrixCollapser.Setter.withCollapserKey(HystrixCollapserKey.Factory.asKey("CacheLookupCollapser"))
                .andScope(HystrixCollapser.Scope.GLOBAL)
                .andCollapserPropertiesDefaults(
                        HystrixCollapserProperties.Setter()
                                .withMaxRequestsInBatch(cacheLookupConfig.getBatchingMaxSize())
                                .withTimerDelayInMilliseconds(cacheLookupConfig.getBatchingTimeInMillis())
                );
    }

    public static HystrixCommand.Setter createCacheLookupCommandSettings(CacheLookupConfig cacheLookupConfig) {
        HystrixCommand.Setter s = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CacheLookupGroup"));
        if(cacheLookupConfig.isUseThreadPool()){
            s.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(cacheLookupConfig.getThreadPoolSize())
                    .withMaxQueueSize(cacheLookupConfig.getThreadPoolQueueSize()));
            s.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                    .withExecutionIsolationThreadTimeoutInMilliseconds(cacheLookupConfig.getLookupTimeoutInMillis()));
            s.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("CacheLookup"));
        } else {
            s.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                            .withExecutionIsolationSemaphoreMaxConcurrentRequests(cacheLookupConfig.getSemaphoreSize())
            );
        }
        s.andCommandKey(HystrixCommandKey.Factory.asKey("CacheLookup"));
        return s;
    }
}
